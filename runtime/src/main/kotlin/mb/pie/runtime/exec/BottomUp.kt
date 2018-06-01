package mb.pie.runtime.exec

import mb.pie.api.*
import mb.pie.api.exec.*
import mb.pie.api.stamp.FileStamper
import mb.pie.api.stamp.OutputStamper
import mb.pie.vfs.path.PPath
import java.util.*
import java.util.concurrent.ConcurrentHashMap

typealias TaskObserver = (Out) -> Unit

class BottomUpExecutorImpl constructor(
  private val taskDefs: TaskDefs,
  private val store: Store,
  private val share: Share,
  private val defaultOutputStamper: OutputStamper,
  private val defaultFileReqStamper: FileStamper,
  private val defaultFileGenStamper: FileStamper,
  private val layerFactory: (Logger) -> Layer,
  private val logger: Logger,
  private val executorLoggerFactory: (Logger) -> ExecutorLogger
) : BottomUpExecutor {
  private val observers = ConcurrentHashMap<TaskKey, TaskObserver>()


  @Throws(ExecException::class, InterruptedException::class)
  override fun <I : In, O : Out> requireTopDown(task: Task<I, O>, cancel: Cancelled): O {
    val session = newSession()
    return session.requireTopDownInitial(task, cancel)
  }

  @Throws(ExecException::class, InterruptedException::class)
  override fun requireBottomUp(changedFiles: Set<PPath>, cancel: Cancelled) {
    if(changedFiles.isEmpty()) return
    val changedRate = changedFiles.size.toFloat() / store.readTxn().use { it.numSourceFiles() }.toFloat()
    if(changedRate > 0.5) {
      val topdownSession = TopDownSessionImpl(taskDefs, store, share, defaultOutputStamper, defaultFileReqStamper, defaultFileGenStamper, layerFactory(logger), logger, executorLoggerFactory(logger))
      for(key in observers.keys) {
        val task = store.readTxn().use { txn -> key.toTask(taskDefs, txn) }
        topdownSession.requireInitial(task, cancel)
        // TODO: observers are not called when using a topdown session.
      }
    } else {
      val session = newSession()
      session.requireBottomUpInitial(changedFiles, cancel)
    }
  }

  override fun hasBeenRequired(key: TaskKey): Boolean {
    return store.readTxn().use { it.output(key) } != null
  }

  override fun setObserver(key: TaskKey, observer: (Out) -> Unit) {
    observers[key] = observer
  }

  override fun removeObserver(key: TaskKey) {
    observers.remove(key)
  }


  @Suppress("MemberVisibilityCanBePrivate")
  fun newSession(): BottomUpSession {
    return BottomUpSession(taskDefs, observers, store, share, defaultOutputStamper, defaultFileReqStamper, defaultFileGenStamper, layerFactory(logger), logger, executorLoggerFactory(logger))
  }
}

open class BottomUpSession(
  private val taskDefs: TaskDefs,
  private val observers: Map<TaskKey, TaskObserver>,
  private val store: Store,
  share: Share,
  defaultOutputStamper: OutputStamper,
  defaultFileReqStamper: FileStamper,
  defaultFileGenStamper: FileStamper,
  private val layer: Layer,
  private val logger: Logger,
  private val executorLogger: ExecutorLogger
) : RequireTask {
  private val visited = mutableMapOf<TaskKey, TaskData<*, *>>()
  private val queue = DistinctPriorityQueue(Comparator { key1, key2 ->
    when {
      key1 == key2 -> 0
      store.readTxn().use { txn -> txn.hasTransitiveTaskReq(key1, key2) } -> 1
      else -> -1
    }
  })
  private val executor = TaskExecutor(visited, store, share, defaultOutputStamper, defaultFileReqStamper, defaultFileGenStamper, layer, logger, executorLogger) { key, data ->
    // Notify observer, if any.
    val observer = observers[key]
    if(observer != null) {
      val output = data.output
      executorLogger.invokeObserverStart(observer, key, output)
      observer.invoke(output)
      executorLogger.invokeObserverEnd(observer, key, output)
    }
  }
  private val shared = TopDownShared(visited, store, layer, executorLogger)


  /**
   * Entry point for top-down builds.
   */
  fun <I : In, O : Out> requireTopDownInitial(task: Task<I, O>, cancel: Cancelled): O {
    try {
      executorLogger.requireTopDownInitialStart(task)
      val key = task.key()
      val output = require(key, task, cancel)
      executorLogger.requireTopDownInitialEnd(task, output)
      return output
    } finally {
      store.sync()
    }
  }

  /**
   * Entry point for bottom-up builds.
   */
  fun requireBottomUpInitial(changedFiles: Set<PPath>, cancel: Cancelled) {
    try {
      executorLogger.requireBottomUpInitialStart(changedFiles)
      scheduleAffectedByFiles(changedFiles)
      execScheduled(cancel)
      executorLogger.requireBottomUpInitialEnd()
    } finally {
      store.sync()
    }
  }


  /**
   * Executes scheduled tasks (and schedules affected tasks) until queue is empty.
   */
  fun execScheduled(cancel: Cancelled) {
    logger.trace("Executing scheduled tasks: $queue")
    while(queue.isNotEmpty()) {
      cancel.throwIfCancelled()
      val key = queue.poll()
      val task = store.readTxn().use { txn -> key.toTask(taskDefs, txn) }
      logger.trace("Polling: ${task.desc(200)}")
      execAndSchedule(key, task, cancel)
    }
  }

  /**
   * Executes given task, and schedules new tasks based on given task's output.
   */
  private fun <I : In, O : Out> execAndSchedule(key: TaskKey, task: Task<I, O>, cancel: Cancelled): TaskData<I, O> {
    val data = exec(key, task, AffectedExecReason(), cancel)
    scheduleAffectedCallersOf(key, data.output)
    return data
  }

  /**
   * Schedules tasks affected by (changes to) files.
   */
  fun scheduleAffectedByFiles(files: Set<PPath>) {
    logger.trace("Scheduling tasks affected by files: $files")
    val affected = store.readTxn().use { txn -> txn.directlyAffectedTaskKeys(files, logger) }
    for(key in affected) {
      logger.trace("- scheduling: $key")
      queue.add(key)
    }
  }

  /**
   * Schedules tasks affected by (changes to the) output of a task.
   */
  private fun scheduleAffectedCallersOf(callee: TaskKey, output: Out) {
    logger.trace("Scheduling tasks affected by output of: ${callee.toShortString(200)}")
    val inconsistentTaskKeys = store.readTxn().use { txn ->
      txn.callersOf(callee).filter { caller ->
        txn.taskReqs(caller).filter { it.calleeEqual(callee) }.all { it.isConsistent(output) }
      }
    }
    for(key in inconsistentTaskKeys) {
      logger.trace("- scheduling: $key")
      queue.add(key)
    }
  }


  /**
   * Require the result of a task.
   */
  override fun <I : In, O : Out> require(key: TaskKey, task: Task<I, O>, cancel: Cancelled): O {
    Stats.addRequires()
    cancel.throwIfCancelled()
    layer.requireTopDownStart(key, task.input)
    executorLogger.requireTopDownStart(task)

    try {
      // If already visited: return cached.
      val visitedData = visited[key]
      if(visitedData != null) {
        val output = visitedData.output.cast<O>()
        executorLogger.requireTopDownEnd(task, output)
        return output
      }

      val existingWrappedOutput = store.readTxn().use { it.output(key) }
      if(existingWrappedOutput != null) {
        // Task is in dependency graph.
        val existingOutput = existingWrappedOutput.output

        // Internal consistency: transient output consistency
        /*
        Required for transient outputs. When a function application has a transient output, its output cannot be persisted
        and instead can only be stored in memory. After a restart of the JVM, this memory must be restored by re-executing
        the function application. This check ensures that this happens.
        */
        run {
          val reason = existingOutput.isTransientInconsistent()
          if(reason != null) {
            val execData = execAndSchedule(key, task, cancel)
            val execOutput = execData.output.cast<O>()
            executorLogger.requireTopDownEnd(task, execOutput)
            return execOutput
          }
        }

        // Task is scheduled to be run, but needs to be run *now*.
        val requireNowData = requireScheduledNow(key, cancel)
        if(requireNowData != null) {
          // Task was affected and has been executed.
          val requireNowOutput = requireNowData.output.cast<O>()
          executorLogger.requireTopDownEnd(task, requireNowOutput)
          return requireNowOutput
        } else {
          // Task was not affected.
          // Notify observer, if any.
          val observer = observers[key]
          if(observer != null) {
            executorLogger.invokeObserverStart(observer, task, existingOutput)
            observer.invoke(existingOutput)
            executorLogger.invokeObserverEnd(observer, task, existingOutput)
          }
          // Return stored result.
          val output = existingOutput.cast<O>()
          executorLogger.requireTopDownEnd(task, output)
          return output
        }
      } else {
        // Task is not in dependency graph: execute. This tasks's output cannot affect other tasks since it is new.
        val reason = NoOutputReason()
        val execData = exec(key, task, reason, cancel)
        val execOutput = execData.output.cast<O>()
        executorLogger.requireTopDownEnd(task, execOutput)
        return execOutput
      }
    } finally {
      layer.requireTopDownEnd(key)
    }
  }

  /**
   * Execute the scheduled dependency of a task, and the task itself, which is required to be run *now*.
   */
  private fun requireScheduledNow(key: TaskKey, cancel: Cancelled): TaskData<*, *>? {
    logger.trace("Executing scheduled (and its dependencies) task NOW: $key")
    while(queue.isNotEmpty()) {
      cancel.throwIfCancelled()
      val txn = store.readTxn()
      val minTaskKey = queue.pollLeastElemLessThanOrEqual(key, txn)
      if(minTaskKey == null) {
        txn.close()
        break
      }
      val minTask = minTaskKey.toTask(taskDefs, txn)
      txn.close()
      logger.trace("- least element less than task: ${minTask.desc()}")
      val data = execAndSchedule(minTaskKey, minTask, cancel)
      if(minTaskKey == key) {
        return data // Task was affected, and has been executed: return result
      }
    }
    return null // Task was not affected: return null
  }


  open fun <I : In, O : Out> exec(key: TaskKey, task: Task<I, O>, reason: ExecReason, cancel: Cancelled): TaskData<I, O> {
    return executor.exec(key, task, reason, this, cancel)
  }
}

class DistinctPriorityQueue(comparator: Comparator<TaskKey>) {
  private val queue = PriorityQueue<TaskKey>(comparator)
  private val set = hashSetOf<TaskKey>()


  fun isNotEmpty(): Boolean {
    return queue.isNotEmpty()
  }

  fun contains(key: TaskKey): Boolean {
    return set.contains(key)
  }

  fun poll(): TaskKey {
    val key = queue.remove()
    set.remove(key)
    return key
  }

  fun pollLeastElemLessThanOrEqual(key: TaskKey, txn: StoreReadTxn): TaskKey? {
    val queueCopy = PriorityQueue(queue)
    while(queueCopy.isNotEmpty()) {
      val queuedKey = queueCopy.poll()
      if(queuedKey == key || txn.hasTransitiveTaskReq(key, queuedKey)) {
        queue.remove(queuedKey)
        set.remove(queuedKey)
        return queuedKey
      }
    }
    return null
  }

  fun add(key: TaskKey) {
    if(set.contains(key)) return
    queue.add(key)
    set.add(key)
  }

  override fun toString() = queue.toString()
}
