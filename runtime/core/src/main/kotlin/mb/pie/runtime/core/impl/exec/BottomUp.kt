package mb.pie.runtime.core.impl.exec

import com.google.inject.Inject
import com.google.inject.Provider
import com.google.inject.assistedinject.Assisted
import mb.pie.runtime.core.*
import mb.pie.runtime.core.exec.BottomUpExecutor
import mb.pie.runtime.core.exec.FuncAppObserver
import mb.pie.runtime.core.impl.*
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
  private val funcs: MutableMap<String, UFunc>
) : BottomUpExecutor {
  private val keyToApp = ConcurrentHashMap<Any, UFuncApp>()
  private val appToObs = ConcurrentHashMap<UFuncApp, FuncAppObserver>()


  override fun setObserver(key: Any, app: UFuncApp, observer: FuncAppObserver) {
    val existingApp = keyToApp[key]
    if(existingApp != null) {
      appToObs.remove(existingApp)
    }
    keyToApp[key] = app
    appToObs[app] = observer
  }

  override fun removeObserver(key: Any) {
    val app = keyToApp[key]
    if(app != null) {
      appToObs.remove(app)
    }
    keyToApp.remove(key)
  }


  @Throws(ExecException::class, InterruptedException::class)
  override fun <I : In, O : Out> requireTopDown(app: FuncApp<I, O>, cancel: Cancelled): O {
    val exec = exec()
    return exec.require(app, cancel).output
  }

  @Throws(ExecException::class, InterruptedException::class)
  override fun requireBottomUp(changedPaths: Set<PPath>, cancel: Cancelled) {
    if(changedPaths.isEmpty()) return
    val changedRate = changedPaths.size.toFloat() / store.readTxn().use { it.numSourceFiles() }.toFloat()
    logger.get().trace("- rate of changed source files: $changedRate")
    if(changedRate > 0.5) {
      logger.get().trace("- rate of changed source files is larger than 50%, running a top-down build on all observed tasks instead")
      val topdownExec = TopDownExecImpl(store, cache, share, layer.get(), logger.get(), funcs)
      for(task in keyToApp.values) {
        topdownExec.requireInitial(task, cancel)
      }
    } else {
      val exec = exec()
      exec.scheduleAffectedByFiles(changedPaths)
      exec.execScheduled(cancel)
    }
  }

  override fun <I : In, O : Out> hasBeenRequired(app: FuncApp<I, O>): Boolean {
    return (cache[app] ?: store.readTxn().use { it.output(app) }) != null
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
  private val cache: Cache,
  share: Share,
  private val layer: Layer,
  private val logger: Logger,
  private val funcs: Map<String, UFunc>,
  private val observers: Map<UFuncApp, FuncAppObserver>
) : Exec, Funcs by FuncsImpl(funcs) {
  private val visited = mutableMapOf<UFuncApp, UFuncAppData>()
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
  private fun execAndSchedule(next: UFuncApp, cancel: Cancelled): UFuncAppData {
    val data = exec(next, InvalidatedExecReason(), cancel)
    scheduleAffectedCallersOf(next, data.output)
    return data
  }

  /**
   * Require the result of a task.
   */
  override fun <I : In, O : Out> require(app: FuncApp<I, O>, cancel: Cancelled): ExecRes<O> {
    Stats.addRequires()
    cancel.throwIfCancelled()
    layer.requireTopDownStart(app)
    logger.requireTopDownStart(app)

    try {
      // If already visited: return cached.
      val visitedData = visited[app]?.cast<O>()
      if(visitedData != null) {
        val res = ExecRes(visitedData.output)
        logger.requireTopDownEnd(app, res)
        return res
      }

      val data = store.readTxn().use { it.data(app) }
      if(data != null) {
        // Task is in dependency graph.
        val (output, _, _, _) = data

        // Internal consistency: transient output consistency
        /*
        Required for transient outputs. When a function application has a transient output, its output cannot be persisted
        and instead can only be stored in memory. After a restart of the JVM, this memory must be restored by re-executing
        the function application. This check ensures that this happens.
        */
        run {
          val reason = output.isTransientInconsistent()
          if(reason != null) {
            val execData = execAndSchedule(app, cancel)
            val res = ExecRes(execData.output.cast<O>(), reason)
            logger.requireTopDownEnd(app, res)
            return res
          }
        }

        // Task is scheduled to be run, but needs to be run *now*.
        val requireNowData = requireScheduledNow(app, cancel)
        if(requireNowData != null) {
          // Task was affected and has been executed.
          val res = ExecRes(requireNowData.output.cast<O>(), InvalidatedExecReason())
          logger.requireTopDownEnd(app, res)
          return res
        } else {
          // Task was not affected.
          // Notify observer, if any.
          val observer = observers[app]
          if(observer != null) {
            logger.invokeObserverStart(observer, app, output)
            observer.invoke(output)
            logger.invokeObserverEnd(observer, app, output)
          }
          // Return stored result.
          val res = ExecRes(data.output.cast<O>())
          logger.requireTopDownEnd(app, res)
          return res
        }
      } else {
        // Task is not in dependency graph: execute. This tasks's output cannot affect other tasks since it is new.
        val reason = NoResultReason()
        val execData = exec(app, reason, cancel)
        val res = ExecRes(execData.output.cast<O>(), reason)
        logger.requireTopDownEnd(app, res)
        return res
      }
    } finally {
      layer.requireTopDownEnd(app)
    }
  }

  /**
   * Execute the scheduled dependency of a task, and the task itself, which is required to be run *now*.
   */
  private fun requireScheduledNow(task: UFuncApp, cancel: Cancelled): UFuncAppData? {
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


  private fun affectedByFiles(files: Set<PPath>): HashSet<UFuncApp> {
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
  private fun scheduleAffectedCallersOf(task: UFuncApp, output: Out) {
    logger.trace("Scheduling tasks affected by output of task: ${task.toShortString(200)}")
    val callers = store.readTxn().use { txn -> txn.callersOf(task) }
    for(caller in callers) {
      // OPTO: prevent creating a read transaction twice? cannot create encompassing txn because queue insertion requires read transaction for sorting.
      val callReqs = store.readTxn().use { txn -> txn.callReqs(caller) }
      val relevantCallReqs = callReqs.filter { it.calleeEqual(task) }
      val consistent = relevantCallReqs.all { it.isConsistent(output) }
      if(!consistent) {
        schedule(caller)
      }
    }
  }

  private fun schedule(task: UFuncApp) {
    logger.trace("- scheduling: ${task.toShortString(200)}")
    queue.add(task)
  }


  internal open fun exec(app: UFuncApp, reason: ExecReason, cancel: Cancelled, useCache: Boolean = false): UFuncAppData {
    return shared.exec(app, reason, cancel, useCache) { appL, cancelL -> this.execInternal(appL, cancelL) }
  }

  internal open fun execInternal(app: UFuncApp, cancel: Cancelled): UFuncAppData {
    return shared.execInternal(app, cancel, this, this) { _, data ->
      // Notify observer, if any.
      val observer = observers[app]
      if(observer != null) {
        val output = data.output
        logger.invokeObserverStart(observer, app, output)
        observer.invoke(output)
        logger.invokeObserverEnd(observer, app, output)
      }
    }
  }
}

class DistinctPriorityQueue(comparator: Comparator<UFuncApp>) {
  private val queue = PriorityQueue<UFuncApp>(comparator)
  private val set = hashSetOf<UFuncApp>()


  fun isNotEmpty(): Boolean {
    return queue.isNotEmpty()
  }

  fun contains(elem: UFuncApp): Boolean {
    return set.contains(elem)
  }

  fun poll(): UFuncApp {
    val elem = queue.remove()
    set.remove(elem)
    return elem
  }

  fun pollLeastElemLessThanOrEqual(other: UFuncApp, txn: StoreReadTxn): UFuncApp? {
    val queueCopy = PriorityQueue(queue)
    while(queueCopy.isNotEmpty()) {
      val elem = queueCopy.poll()
      if(elem == other || hasCallReq(other, elem!!, txn)) {
        queue.remove(elem)
        set.remove(elem)
        return elem
      }
    }
    return null
  }

  fun add(elem: UFuncApp) {
    if(set.contains(elem)) return
    queue.add(elem)
    set.add(elem)
  }

  fun addAll(elems: Collection<UFuncApp>) {
    for(elem in elems) {
      add(elem)
    }
  }

  override fun toString(): String {
    return queue.toString()
  }
}
