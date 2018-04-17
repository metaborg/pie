package mb.pie.runtime.core.impl.exec

import com.google.inject.Inject
import com.google.inject.Provider
import com.google.inject.assistedinject.Assisted
import mb.pie.runtime.core.*
import mb.pie.runtime.core.exec.BottomUpTopsortExecutor
import mb.pie.runtime.core.exec.FuncAppObserver
import mb.pie.runtime.core.impl.*
import mb.util.async.Cancelled
import mb.vfs.path.PPath
import java.util.*
import java.util.concurrent.ConcurrentHashMap


class BottomUpTopsortExecutorImpl @Inject constructor(
  @Assisted private val store: Store,
  @Assisted private val cache: Cache,
  private val share: Share,
  private val layer: Provider<Layer>,
  private val logger: Provider<Logger>,
  private val funcs: MutableMap<String, UFunc>
) : BottomUpTopsortExecutor {
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
    val output = exec.require(app, cancel).output
    exec.execScheduled(cancel)
    return output
  }

  @Throws(ExecException::class, InterruptedException::class)
  override fun requireBottomUp(changedPaths: List<PPath>, cancel: Cancelled) {
    if(changedPaths.isEmpty()) return
    val exec = exec()
    exec.scheduleAffectedByFiles(changedPaths)
    exec.execScheduled(cancel)
  }

  override fun <I : In, O : Out> hasBeenRequired(app: FuncApp<I, O>): Boolean {
    return (cache[app] ?: store.readTxn().use { it.output(app) }) != null
  }


  private fun exec(): BottomUpTopsortExec {
    return BottomUpTopsortExec(store, cache, share, layer.get(), logger.get(), funcs, appToObs)
  }


  override fun dropStore() {
    store.writeTxn().use { it.drop() }
    store.sync()
  }

  override fun dropCache() {
    cache.drop()
  }
}

open class BottomUpTopsortExec(
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
      store.readTxn().use { txn -> hasCallReq(app1, app2, this, txn) } -> 1
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
      execAndSchedule(next, true, cancel)
    }
  }

  /**
   * Executes given task, and schedules new tasks based on given task's generated paths and output.
   */
  private fun execAndSchedule(next: UFuncApp, scheduleCallers: Boolean, cancel: Cancelled): UFuncAppData {
    val data = exec(next, InvalidatedExecReason(), cancel)
    scheduleAffectedByFiles(data.pathGens.map { it.path })
    if(scheduleCallers) {
      scheduleAffectedCallersOf(next, data.output)
    }
    return data
  }

  /**
   * Require the result of a task.
   */
  override fun <I : In, O : Out> require(task: FuncApp<I, O>, cancel: Cancelled): ExecRes<O> {
    Stats.addRequires()
    cancel.throwIfCancelled()
    layer.requireTopDownStart(task)
    logger.requireTopDownStart(task)

    try {
      // If already visited: return cached.
      val visitedData = visited[task]?.cast<O>()
      if(visitedData != null) {
        val res = ExecRes(visitedData.output)
        logger.requireTopDownEnd(task, res)
        return res
      }

      val data = store.readTxn().use { it.data(task) }
      if(data != null) {
        // Task is in dependency graph.
        val (output, _, _, pathGens) = data
        if(queue.contains(task)) {
          // Task is scheduled to be run, but needs to be run *now*.
          val execData = requireScheduledNow(task, cancel)
          val res = ExecRes(execData.output.cast<O>(), InvalidatedExecReason())
          logger.requireTopDownEnd(task, res)
          return res
        } else {
          // Internal consistency: transient output consistency
          /*
          Required for transient outputs. When a function application has a transient output, its output cannot be persisted
          and instead can only be stored in memory. After a restart of the JVM, this memory must be restored by re-executing
          the function application. This check ensures that this happens.
          */
          run {
            val reason = output.isTransientInconsistent()
            if(reason != null) {
              val execData = execAndSchedule(task, false, cancel)
              val res = ExecRes(execData.output.cast<O>(), reason)
              logger.requireTopDownEnd(task, res)
              return res
            }
          }

          // Internal consistency: path generates
          /*
          Required for overlapping generated paths. When two function applications generate the same path, and those
          function applications overlap (meaning they are allowed to both generate the same path), we must ensure that the
          path is generated by the correct function application. This check ensures that by triggering re-execution of the
          function application if one of its generated paths is inconsistent.
          */
          for(pathGen in pathGens) {
            logger.checkPathGenStart(task, pathGen)
            val reason = pathGen.checkConsistency()
            if(reason != null) {
              // If a generated file is outdated (i.e., its stamp changed): rebuild
              logger.checkPathGenEnd(task, pathGen, reason)
              val execData = execAndSchedule(task, false, cancel)
              val res = ExecRes(execData.output.cast<O>(), reason)
              logger.requireTopDownEnd(task, res)
              return res
            } else {
              logger.checkPathGenEnd(task, pathGen, null)
            }
          }

          // Task has already been executed (scheduled before), or does not need to be executed (not affected): return stored.
          logger.trace("Task already executed, or no execution necessary, returning stored/cached output")

          // Notify observer, if any.
          val observer = observers[task]
          if(observer != null) {
            logger.invokeObserverStart(observer, task, output)
            observer.invoke(output)
            logger.invokeObserverEnd(observer, task, output)
          }
          val res = ExecRes(output.cast<O>())
          logger.requireTopDownEnd(task, res)
          return res
        }
      } else {
        // Task is not in dependency graph: execute and schedule new tasks based on file changes. This tasks's output cannot affect other
        // tasks since it is new.
        val execData = execAndSchedule(task, false, cancel)
        val res = ExecRes(execData.output.cast<O>(), NoResultReason())
        logger.requireTopDownEnd(task, res)
        return res
      }
    } finally {
      layer.requireTopDownEnd(task)
    }
  }

  /**
   * Execute the scheduled dependency of a task, and the task itself, which is required to be run *now*.
   */
  private fun requireScheduledNow(task: UFuncApp, cancel: Cancelled): UFuncAppData {
    logger.trace("Executing scheduled (and its dependencies) task NOW: ${task.toShortString(200)}")
    while(queue.isNotEmpty()) {
      cancel.throwIfCancelled()
      val min = store.readTxn().use {
        queue.pollLeastElemLessThanOrEqual(task, this, it)
          ?: throw RuntimeException("Cannot find task smaller or equal to: ${task.toShortString(200)}")
      }
      logger.trace("- least element less than task: ${min.toShortString(200)}")
      val data = execAndSchedule(min, true, cancel)
      if(min == task) {
        return data
      }
    }
    throw RuntimeException("Did not find scheduled task equal to: ${task.toShortString(200)}")
  }

  /**
   * Schedules tasks affected by (changes to) files.
   */
  fun scheduleAffectedByFiles(files: List<PPath>) {
    logger.trace("Scheduling tasks affected by files: $files")
    val affected = store.readTxn().use { txn -> directlyAffectedApps(files, txn, logger) }
    for(task in affected) {
      logger.trace("- scheduling: ${task.toShortString(200)}")
      queue.addAll(affected)
    }
  }

  /**
   * Schedules tasks affected by (changes to the) output of a task.
   */
  fun scheduleAffectedCallersOf(task: UFuncApp, output: Out) {
    logger.trace("Scheduling tasks affected by output of task: ${task.toShortString(200)}")
    val callers = store.readTxn().use { txn -> txn.callersOf(task) }
    for(caller in callers) {
      // OPTO: prevent creating a read transaction twice? cannot create encompassing txn because queue insertion requires read transaction for sorting.
      val callReqs = store.readTxn().use { txn -> txn.callReqs(caller) }
      val relevantCallReqs = callReqs.filter { it.equalsOrOverlaps(task, this) }
      val consistent = relevantCallReqs.all { it.isConsistent(output) }
      if(!consistent) {
        logger.trace("- scheduling: ${caller.toShortString(200)}")
        queue.add(caller)
      }
    }
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

  fun pollLeastElemLessThanOrEqual(other: UFuncApp, funcs: Funcs, txn: StoreReadTxn): UFuncApp? {
    val array = queue.toArray(Array<UFuncApp?>(queue.size) { _ -> null })
    Arrays.sort(array, queue.comparator())
    for(elem in array) {
      if(elem == other || hasCallReq(other, elem!!, funcs, txn)) {
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