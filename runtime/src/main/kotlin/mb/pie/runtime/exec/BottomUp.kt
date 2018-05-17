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
  private val cache: Cache,
  private val share: Share,
  private val defaultOutputStamper: OutputStamper,
  private val defaultFileReqStamper: FileStamper,
  private val defaultFileGenStamper: FileStamper,
  private val layerFactory: (Logger) -> Layer,
  private val logger: Logger,
  private val executorLoggerFactory: (Logger) -> ExecutorLogger
) : BottomUpExecutor {
  private val keyToApp = ConcurrentHashMap<Any, UTask>()
  private val appToObs = ConcurrentHashMap<UTask, TaskObserver>()


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
      val topdownSession = TopDownSessionImpl(taskDefs, store, cache, share, defaultOutputStamper, defaultFileReqStamper, defaultFileGenStamper, layerFactory(logger), logger, executorLoggerFactory(logger))
      for(task in keyToApp.values) {
        // TODO: observers are not called when using a topdown session.
        topdownSession.requireInitial(task, cancel)
      }
    } else {
      val session = newSession()
      session.requireBottomUpInitial(changedFiles, cancel)
    }
  }

  override fun <I : In, O : Out> hasBeenRequired(task: Task<I, O>): Boolean {
    return (cache[task] ?: store.readTxn().use { it.output(task) }) != null
  }

  override fun setObserver(key: Any, task: UTask, observer: TaskObserver) {
    val existingApp = keyToApp[key]
    if(existingApp != null) {
      appToObs.remove(existingApp)
    }
    keyToApp[key] = task
    appToObs[task] = observer
  }

  override fun removeObserver(key: Any) {
    val app = keyToApp[key]
    if(app != null) {
      appToObs.remove(app)
    }
    keyToApp.remove(key)
  }


  @Suppress("MemberVisibilityCanBePrivate")
  internal fun newSession(): BottomUpSession {
    return BottomUpSession(taskDefs, appToObs, store, cache, share, defaultOutputStamper, defaultFileReqStamper, defaultFileGenStamper, layerFactory(logger), logger, executorLoggerFactory(logger))
  }
}

open class BottomUpSession(
  taskDefs: TaskDefs,
  private val observers: Map<UTask, TaskObserver>,
  private val store: Store,
  cache: Cache,
  share: Share,
  defaultOutputStamper: OutputStamper,
  defaultFileReqStamper: FileStamper,
  defaultFileGenStamper: FileStamper,
  private val layer: Layer,
  private val logger: Logger,
  private val executorLogger: ExecutorLogger
) : RequireTask {
  private val visited = mutableMapOf<UTask, UTaskData>()
  private val queue = DistinctPriorityQueue(Comparator { app1, app2 ->
    when {
      app1 == app2 -> 0
      store.readTxn().use { txn -> hasCallReq(app1, app2, txn) } -> 1
      else -> -1
    }
  })
  private val shared = TopDownExecShared(taskDefs, visited, store, cache, share, defaultOutputStamper, defaultFileReqStamper, defaultFileGenStamper, layer, logger, executorLogger)


  /**
   * Entry point for top-down builds.
   */
  fun <I : In, O : Out> requireTopDownInitial(task: Task<I, O>, cancel: Cancelled): O {
    try {
      executorLogger.requireTopDownInitialStart(task)
      val info = require(task, cancel)
      executorLogger.requireTopDownInitialEnd(task, info)
      return info
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
  internal fun execScheduled(cancel: Cancelled) {
    logger.trace("Executing scheduled tasks: $queue")
    while(queue.isNotEmpty()) {
      cancel.throwIfCancelled()
      val next = queue.poll()
      logger.trace("Polling: ${next.toShortString(200)}")
      execAndSchedule(next, cancel)
    }
  }

  /**
   * Executes given task, and schedules new tasks based on given task's output.
   */
  private fun execAndSchedule(next: UTask, cancel: Cancelled): UTaskData {
    val data = exec(next, InvalidatedExecReason(), cancel)
    scheduleAffectedCallersOf(next, data.output)
    return data
  }

  /**
   * Schedules tasks affected by (changes to) files.
   */
  internal fun scheduleAffectedByFiles(files: Set<PPath>) {
    logger.trace("Scheduling tasks affected by files: $files")
    val affected = store.readTxn().use { txn -> directlyAffectedApps(files, txn, logger) }
    for(task in affected) {
      logger.trace("- scheduling: ${task.toShortString(200)}")
      queue.add(task)
    }
  }

  /**
   * Schedules tasks affected by (changes to the) output of a task.
   */
  private fun scheduleAffectedCallersOf(task: UTask, output: Out) {
    logger.trace("Scheduling tasks affected by output of task: ${task.toShortString(200)}")
    val callers = store.readTxn().use { txn -> txn.callersOf(task) }
    for(caller in callers) {
      // OPTO: prevent creating a read transaction twice? cannot create encompassing txn because queue insertion requires read transaction for sorting.
      val callReqs = store.readTxn().use { txn -> txn.taskReqs(caller) }
      val relevantCallReqs = callReqs.filter { it.calleeEqual(task) }
      val consistent = relevantCallReqs.all { it.isConsistent(output) }
      if(!consistent) {
        logger.trace("- scheduling: ${caller.toShortString(200)}")
        queue.add(caller)
      }
    }
  }


  /**
   * Require the result of a task.
   */
  override fun <I : In, O : Out> require(task: Task<I, O>, cancel: Cancelled): O {
    Stats.addRequires()
    cancel.throwIfCancelled()
    layer.requireTopDownStart(task)
    executorLogger.requireTopDownStart(task)

    try {
      // If already visited: return cached.
      val visitedData = visited[task]?.cast<O>()
      if(visitedData != null) {
        val output = visitedData.output
        executorLogger.requireTopDownEnd(task, output)
        return output
      }

      val data = store.readTxn().use { it.data(task) }
      if(data != null) {
        // Task is in dependency graph.
        val (existingOutput, _, _, _) = data

        // Internal consistency: transient output consistency
        /*
        Required for transient outputs. When a function application has a transient output, its output cannot be persisted
        and instead can only be stored in memory. After a restart of the JVM, this memory must be restored by re-executing
        the function application. This check ensures that this happens.
        */
        run {
          val reason = existingOutput.isTransientInconsistent()
          if(reason != null) {
            val execData = execAndSchedule(task, cancel)
            val execOutput = execData.output.cast<O>()
            executorLogger.requireTopDownEnd(task, execOutput)
            return execOutput
          }
        }

        // Task is scheduled to be run, but needs to be run *now*.
        val requireNowData = requireScheduledNow(task, cancel)
        if(requireNowData != null) {
          // Task was affected and has been executed.
          val requireNowOutput = requireNowData.output.cast<O>()
          executorLogger.requireTopDownEnd(task, requireNowOutput)
          return requireNowOutput
        } else {
          // Task was not affected.
          // Notify observer, if any.
          val observer = observers[task]
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
        val reason = NoResultReason()
        val execData = exec(task, reason, cancel)
        val execOutput = execData.output.cast<O>()
        executorLogger.requireTopDownEnd(task, execOutput)
        return execOutput
      }
    } finally {
      layer.requireTopDownEnd(task)
    }
  }

  /**
   * Execute the scheduled dependency of a task, and the task itself, which is required to be run *now*.
   */
  private fun requireScheduledNow(task: UTask, cancel: Cancelled): UTaskData? {
    logger.trace("Executing scheduled (and its dependencies) task NOW: ${task.toShortString(200)}")
    while(queue.isNotEmpty()) {
      cancel.throwIfCancelled()
      val min = store.readTxn().use {
        queue.pollLeastElemLessThanOrEqual(task, it)
      } ?: break
      logger.trace("- least element less than task: ${min.toShortString(200)}")
      val data = execAndSchedule(min, cancel)
      if(min == task) {
        return data // Task was affected, and has been executed: return result
      }
    }
    return null // Task was not affected: return null
  }


  internal open fun exec(task: UTask, reason: ExecReason, cancel: Cancelled, useCache: Boolean = false): UTaskData {
    return shared.exec(task, reason, cancel, useCache) { appL, cancelL -> this.execInternal(appL, cancelL) }
  }

  internal open fun execInternal(task: UTask, cancel: Cancelled): UTaskData {
    return shared.execInternal(task, cancel, this) { _, data ->
      // Notify observer, if any.
      val observer = observers[task]
      if(observer != null) {
        val output = data.output
        executorLogger.invokeObserverStart(observer, task, output)
        observer.invoke(output)
        executorLogger.invokeObserverEnd(observer, task, output)
      }
    }
  }
}

class DistinctPriorityQueue(comparator: Comparator<UTask>) {
  private val queue = PriorityQueue<UTask>(comparator)
  private val set = hashSetOf<UTask>()


  fun isNotEmpty(): Boolean {
    return queue.isNotEmpty()
  }

  fun contains(task: UTask): Boolean {
    return set.contains(task)
  }

  fun poll(): UTask {
    val task = queue.remove()
    set.remove(task)
    return task
  }

  fun pollLeastElemLessThanOrEqual(other: UTask, txn: StoreReadTxn): UTask? {
    val queueCopy = PriorityQueue(queue)
    while(queueCopy.isNotEmpty()) {
      val task = queueCopy.poll()
      if(task == other || hasCallReq(other, task!!, txn)) {
        queue.remove(task)
        set.remove(task)
        return task
      }
    }
    return null
  }

  fun add(task: UTask) {
    if(set.contains(task)) return
    queue.add(task)
    set.add(task)
  }

  fun addAll(tasks: Collection<UTask>) {
    for(elem in tasks) {
      add(elem)
    }
  }

  override fun toString(): String {
    return queue.toString()
  }
}
