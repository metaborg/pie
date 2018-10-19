package mb.pie.runtime.exec

import mb.pie.api.*
import mb.pie.api.exec.*
import mb.pie.api.stamp.FileStamper
import mb.pie.api.stamp.OutputStamper

class TopDownExecutorImpl constructor(
  private val taskDefs: TaskDefs,
  private val store: Store,
  private val share: Share,
  private val defaultOutputStamper: OutputStamper,
  private val defaultFileReqStamper: FileStamper,
  private val defaultFileGenStamper: FileStamper,
  private val layerFactory: (Logger) -> Layer,
  private val logger: Logger,
  private val executorLoggerFactory: (Logger) -> ExecutorLogger
) : TopDownExecutor {
  override fun newSession(): TopDownSession {
    return TopDownSessionImpl(taskDefs, store, share, defaultOutputStamper, defaultFileReqStamper, defaultFileGenStamper, layerFactory(logger), logger, executorLoggerFactory(logger))
  }
}

open class TopDownSessionImpl(
  taskDefs: TaskDefs,
  private val store: Store,
  share: Share,
  defaultOutputStamper: OutputStamper,
  defaultFileReqStamper: FileStamper,
  defaultFileGenStamper: FileStamper,
  private val layer: Layer,
  logger: Logger,
  private val executorLogger: ExecutorLogger
) : TopDownSession, RequireTask {
  private val visited = mutableMapOf<TaskKey, TaskData<*, *>>()
  private val executor = TaskExecutor(taskDefs, visited, store, share, defaultOutputStamper, defaultFileReqStamper, defaultFileGenStamper, layer, logger, executorLogger, null)
  private val requireShared = RequireShared(taskDefs, visited, store, executorLogger)


  override fun <I : In, O : Out> requireInitial(task: Task<I, O>): O {
    return requireInitial(task, NullCancelled())
  }

  override fun <I : In, O : Out> requireInitial(task: Task<I, O>, cancel: Cancelled): O {
    try {
      val key = task.key()
      executorLogger.requireTopDownInitialStart(key, task)
      val output = require(key, task, cancel)
      executorLogger.requireTopDownInitialEnd(key, task, output)
      return output
    } finally {
      store.sync()
    }
  }

  override fun <I : In, O : Out> require(key: TaskKey, task: Task<I, O>, cancel: Cancelled): O {
    cancel.throwIfCancelled()
    Stats.addRequires()
    layer.requireTopDownStart(key, task.input)
    executorLogger.requireTopDownStart(key, task)
    try {
      val (data, wasExecuted) = getData(key, task, cancel)
      val output = data.output
      if(!wasExecuted) {
        // Validate well-formedness of the dependency graph.
        store.readTxn().use { layer.validatePostWrite(key, data, it) }
        // Mark task as visited.
        visited[key] = data
      }
      executorLogger.requireTopDownEnd(key, task, output)
      return output
    } finally {
      layer.requireTopDownEnd(key)
    }
  }

  data class DataW<I : In, O : Out>(val data: TaskData<I, O>, val executed: Boolean) {
    constructor(data: TaskData<I, O>) : this(data, true)
  }

  /**
   * Get data for given task/key, either by getting existing data or through execution.
   */
  private fun <I : In, O : Out> getData(key: TaskKey, task: Task<I, O>, cancel: Cancelled): DataW<I, O> {
    // Check if task was already visited this execution. Return immediately if so.
    val visitedData = requireShared.dataFromVisited(key)
    if(visitedData != null) {
      return DataW(visitedData.cast<I, O>(), false)
    }

    // Check if data is stored for task. Execute if not.
    val storedData = requireShared.dataFromStore(key)
    if(storedData == null) {
      return DataW(exec(key, task, NoData(), cancel))
    }

    // Check consistency of task.
    val existingData = storedData.cast<I, O>()
    val (input, output, taskReqs, fileReqs, fileGens) = existingData

    // Internal consistency: input changes.
    with(requireShared.checkInput(input, task)) {
      if(this != null) {
        return DataW(exec(key, task, this, cancel))
      }
    }

    // Internal consistency: transient output consistency.
    with(requireShared.checkOutputConsistency(output)) {
      if(this != null) {
        return DataW(exec(key, task, this, cancel))
      }
    }

    // Internal consistency: file requirements.
    for(fileReq in fileReqs) {
      with(requireShared.checkFileReq(key, task, fileReq)) {
        if(this != null) {
          return DataW(exec(key, task, this, cancel))
        }
      }
    }

    // Internal consistency: file generates.
    for(fileGen in fileGens) {
      with(requireShared.checkFileGen(key, task, fileGen)) {
        if(this != null) {
          return DataW(exec(key, task, this, cancel))
        }
      }
    }

    // Total consistency: call requirements.
    for(taskReq in taskReqs) {
      with(requireShared.checkTaskReq(key, task, taskReq, this, cancel)) {
        if(this != null) {
          return DataW(exec(key, task, this, cancel))
        }
      }
    }

    // Task is consistent.
    return DataW(existingData, false)
  }

  open fun <I : In, O : Out> exec(key: TaskKey, task: Task<I, O>, reason: ExecReason, cancel: Cancelled): TaskData<I, O> {
    return executor.exec(key, task, reason, this, cancel)
  }
}
