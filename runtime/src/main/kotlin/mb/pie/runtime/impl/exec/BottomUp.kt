package mb.pie.runtime.impl.exec

import com.google.inject.Inject
import com.google.inject.Provider
import com.google.inject.assistedinject.Assisted
import mb.pie.runtime.*
import mb.pie.runtime.exec.BottomUpExecutor
import mb.pie.runtime.exec.TaskObserver
import mb.pie.runtime.impl.*
import mb.util.async.Cancelled
import mb.vfs.path.PPath
import java.util.*
import java.util.concurrent.ConcurrentHashMap


class BottomUpExecutorImpl @Inject constructor(
  @Assisted private val store: Store,
  @Assisted private val cache: Cache,
  private val share: Share,
  private val layer: Provider<Layer>,
  private val logger: Provider<Logger>,
  private val funcs: MutableMap<String, UTaskDef>
) : BottomUpExecutor {
  private val keyToApp = ConcurrentHashMap<Any, UTask>()
  private val appToObs = ConcurrentHashMap<UTask, TaskObserver>()


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


  @Throws(ExecException::class, InterruptedException::class)
  override fun <I : In, O : Out> requireTopDown(task: Task<I, O>, cancel: Cancelled): O {
    val exec = exec()
    return exec.require(task, cancel)
  }

  @Throws(ExecException::class, InterruptedException::class)
  override fun requireBottomUp(changedFiles: Set<PPath>, cancel: Cancelled) {
    if(changedFiles.isEmpty()) return
    val changedRate = changedFiles.size.toFloat() / store.readTxn().use { it.numSourceFiles() }.toFloat()
    logger.get().trace("- rate of changed source files: $changedRate")
    if(changedRate > 0.5) {
      logger.get().trace("- rate of changed source files is larger than 50%, running a top-down build on all observed tasks instead")
      val topdownExec = TopDownExecImpl(store, cache, share, layer.get(), logger.get(), funcs)
      for(task in keyToApp.values) {
        topdownExec.requireInitial(task, cancel)
      }
    } else {
      val exec = exec()
      exec.scheduleAffectedByFiles(changedFiles)
      exec.execScheduled(cancel)
    }
  }

  override fun <I : In, O : Out> hasBeenRequired(task: Task<I, O>): Boolean {
    return (cache[task] ?: store.readTxn().use { it.output(task) }) != null
  }


  private fun exec(): BottomUpExec {
    return BottomUpExec(store, cache, share, layer.get(), logger.get(), funcs, appToObs)
  }


  override fun dropStore() {
    store.writeTxn().use { it.drop() }
    store.sync()
  }

  override fun dropCache() {
    cache.drop()
  }
}

open class BottomUpExec(
  private val store: Store,
  cache: Cache,
  share: Share,
  private val layer: Layer,
  private val logger: Logger,
  private val funcs: Map<String, UTaskDef>,
  private val observers: Map<UTask, TaskObserver>
) : RequireTask, TaskDefs by TaskDefsImpl(funcs) {
  private val visited = mutableMapOf<UTask, UTaskData>()
  private val queue = DistinctPriorityQueue(Comparator { app1, app2 ->
    when {
      app1 == app2 -> 0
      store.readTxn().use { txn -> hasCallReq(app1, app2, txn) } -> 1
      else -> -1
    }
  })
  private val shared = TopDownExecShared(store, cache, share, layer, logger, visited)


  /**
   * Executes scheduled tasks (and schedules affected tasks) until queue is empty.
   */
  fun execScheduled(cancel: Cancelled) {
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
   * Require the result of a task.
   */
  override fun <I : In, O : Out> require(task: Task<I, O>, cancel: Cancelled): O {
    Stats.addRequires()
    cancel.throwIfCancelled()
    layer.requireTopDownStart(task)
    logger.requireTopDownStart(task)

    try {
      // If already visited: return cached.
      val visitedData = visited[task]?.cast<O>()
      if(visitedData != null) {
        val output = visitedData.output
        logger.requireTopDownEnd(task, output)
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
            logger.requireTopDownEnd(task, execOutput)
            return execOutput
          }
        }

        // Task is scheduled to be run, but needs to be run *now*.
        val requireNowData = requireScheduledNow(task, cancel)
        if(requireNowData != null) {
          // Task was affected and has been executed.
          val requireNowOutput = requireNowData.output.cast<O>();
          logger.requireTopDownEnd(task, requireNowOutput)
          return requireNowOutput
        } else {
          // Task was not affected.
          // Notify observer, if any.
          val observer = observers[task]
          if(observer != null) {
            logger.invokeObserverStart(observer, task, existingOutput)
            observer.invoke(existingOutput)
            logger.invokeObserverEnd(observer, task, existingOutput)
          }
          // Return stored result.
          val output = existingOutput.cast<O>()
          logger.requireTopDownEnd(task, output)
          return output
        }
      } else {
        // Task is not in dependency graph: execute. This tasks's output cannot affect other tasks since it is new.
        val reason = NoResultReason()
        val execData = exec(task, reason, cancel)
        val execOutput = execData.output.cast<O>()
        logger.requireTopDownEnd(task, execOutput)
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


  private fun affectedByFiles(files: Set<PPath>): HashSet<UTask> {
    return store.readTxn().use { txn -> directlyAffectedApps(files, txn, logger) }
  }

  /**
   * Schedules tasks affected by (changes to) files.
   */
  fun scheduleAffectedByFiles(files: Set<PPath>) {
    logger.trace("Scheduling tasks affected by files: $files")
    val affected = affectedByFiles(files)
    for(task in affected) {
      schedule(task)
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
        schedule(caller)
      }
    }
  }

  private fun schedule(task: UTask) {
    logger.trace("- scheduling: ${task.toShortString(200)}")
    queue.add(task)
  }


  internal open fun exec(task: UTask, reason: ExecReason, cancel: Cancelled, useCache: Boolean = false): UTaskData {
    return shared.exec(task, reason, cancel, useCache) { appL, cancelL -> this.execInternal(appL, cancelL) }
  }

  internal open fun execInternal(task: UTask, cancel: Cancelled): UTaskData {
    return shared.execInternal(task, cancel, this, this) { _, data ->
      // Notify observer, if any.
      val observer = observers[task]
      if(observer != null) {
        val output = data.output
        logger.invokeObserverStart(observer, task, output)
        observer.invoke(output)
        logger.invokeObserverEnd(observer, task, output)
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
