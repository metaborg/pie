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
  private val taskDefs: TaskDefs,
  private val store: Store,
  share: Share,
  defaultOutputStamper: OutputStamper,
  defaultFileReqStamper: FileStamper,
  defaultFileGenStamper: FileStamper,
  private val layer: Layer,
  logger: Logger,
  private val executorLogger: ExecutorLogger
) : TopDownSession, RequireTask {
  private val visited = mutableMapOf<TaskKey, UTaskData>()
  private val executor = TaskExecutor(visited, store, share, defaultOutputStamper, defaultFileReqStamper, defaultFileGenStamper, layer, logger, executorLogger, null)
  private val shared = TopDownShared(visited, store, layer, executorLogger)


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

    try {
      val outputOrData = shared.topdownPrelude(key, task)
      if(outputOrData.visited != null) {
        // Task was already visited this execution, return cached output.
        return outputOrData.visited.cast<O>()
      }
      val data = outputOrData.data

      // Check if re-execution is necessary.
      if(data == null) {
        // No cached or stored output was found: re-execute.
        val reason = NoOutputReason()
        val execData = exec(key, task, reason, cancel)
        val output = execData.output
        executorLogger.requireTopDownEnd(key, task, output)
        return output
      }
      val (_, existingOutput, taskReqs, pathReqs, pathGens) = data

      // Check for inconsistencies and re-execute when found.
      run {
        // Internal consistency: transient output consistency
        val reason = existingOutput.isTransientInconsistent()
        if(reason != null) {
          val execData = exec(key, task, reason, cancel)
          val execOutput = execData.output.cast<O>()
          executorLogger.requireTopDownEnd(key, task, execOutput)
          return execOutput
        }
      }

      // Internal consistency: file requirements
      for(pathReq in pathReqs) {
        executorLogger.checkFileReqStart(key, task, pathReq)
        val reason = pathReq.checkConsistency()
        if(reason != null) {
          // If a required file is outdated (i.e., its stamp changed): rebuild
          executorLogger.checkFileReqEnd(key, task, pathReq, reason)
          val execData = exec(key, task, reason, cancel)
          val execOutput = execData.output.cast<O>()
          executorLogger.requireTopDownEnd(key, task, execOutput)
          return execOutput
        } else {
          executorLogger.checkFileReqEnd(key, task, pathReq, null)
        }
      }

      // Internal consistency: file generates
      for(pathGen in pathGens) {
        executorLogger.checkFileGenStart(key, task, pathGen)
        val reason = pathGen.checkConsistency()
        if(reason != null) {
          // If a generated file is outdated (i.e., its stamp changed): rebuild
          executorLogger.checkFileGenEnd(key, task, pathGen, reason)
          val execData = exec(key, task, reason, cancel)
          val execOutput = execData.output.cast<O>()
          executorLogger.requireTopDownEnd(key, task, execOutput)
          return execOutput
        } else {
          executorLogger.checkFileGenEnd(key, task, pathGen, null)
        }
      }

      // Total consistency: call requirements
      for(taskReq in taskReqs) {
        val calleeKey = taskReq.callee
        val calleeTask = store.readTxn().use { txn -> calleeKey.toTask(taskDefs, txn) }
        val calleeOutput = require(calleeKey, calleeTask, cancel)
        executorLogger.checkTaskReqStart(key, task, taskReq)
        val reason = taskReq.checkConsistency(calleeOutput)
        executorLogger.checkTaskReqEnd(key, task, taskReq, reason)
        if(reason != null) {
          val execData = exec(key, task, reason, cancel)
          val execOutput = execData.output.cast<O>()
          executorLogger.requireTopDownEnd(key, task, execOutput)
          return execOutput
        }
      }

      // No inconsistencies found
      // Validate well-formedness of the dependency graph
      store.readTxn().use { layer.validatePostWrite(key, data, it) }
      // Cache and mark as visited
      visited[key] = data
      // Reuse existing result
      executorLogger.requireTopDownEnd(key, task, existingOutput)
      return existingOutput.cast<O>()
    } finally {
      layer.requireTopDownEnd(key)
    }
  }

  open fun <I : In, O : Out> exec(key: TaskKey, task: Task<I, O>, reason: ExecReason, cancel: Cancelled): TaskData<I, O> {
    return executor.exec(key, task, reason, this, cancel)
  }
}
