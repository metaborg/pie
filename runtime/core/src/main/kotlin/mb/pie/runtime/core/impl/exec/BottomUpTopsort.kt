package mb.pie.runtime.core.impl.exec

import com.google.inject.Inject
import com.google.inject.Provider
import com.google.inject.assistedinject.Assisted
import mb.pie.runtime.core.*
import mb.pie.runtime.core.exec.BottomUpTopsortExecutor
import mb.pie.runtime.core.exec.FuncAppObserver
import mb.pie.runtime.core.impl.*
import mb.util.async.Cancelled
import mb.util.async.NullCancelled
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
    val exec = exec(false)
    return exec.requireTopDownInitial(app, cancel).output
  }

  @Throws(ExecException::class, InterruptedException::class)
  override fun requireBottomUp(changedPaths: List<PPath>, cancel: Cancelled) {
    val exec = exec(true)
    exec.requireBottomUpInitial(changedPaths, cancel)
  }

  override fun <I : In, O : Out> hasBeenRequired(app: FuncApp<I, O>): Boolean {
    return (cache[app] ?: store.readTxn().use { it.output(app) }) != null
  }


  fun exec(hybrid: Boolean): BottomUpTopsortExec {
    return BottomUpTopsortExec(store, cache, share, layer.get(), logger.get(), funcs, appToObs, hybrid)
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
  private val observers: Map<UFuncApp, FuncAppObserver>,
  private val hybrid: Boolean
) : Exec, Funcs by FuncsImpl(funcs) {
  private val visited = mutableMapOf<UFuncApp, UFuncAppData>()
  private val queue = PriorityQueue<UFuncApp>(Comparator<UFuncApp> { app1, app2 ->
    when {
      app1 == app2 -> 0
      store.readTxn().use { txn -> hasCallReq(app1, app2, this, txn) } -> 1
      else -> -1
    }
  })
  private val queuedSet = mutableSetOf<UFuncApp>()
  private val shared = TopDownExecShared(store, cache, share, layer, logger, visited)


  /**
   * Require the result of an observable function application in a top-down manner.
   */
  fun <I : In, O : Out> requireTopDownInitial(app: FuncApp<I, O>, cancel: Cancelled = NullCancelled()): ExecRes<O> {
    try {
      logger.requireTopDownInitialStart(app)
      val res = require(app, cancel)
      logger.requireTopDownInitialEnd(app, res)
      return res
    } finally {
      store.sync()
    }
  }

  /**
   * Execute function applications affected by a changed path in a bottom-up manner.
   */
  fun requireBottomUpInitial(changedPaths: List<PPath>, cancel: Cancelled) {
    if(changedPaths.isEmpty()) return

    // Find all function applications that are affected by changed paths.
    val affected = store.readTxn().use { txn -> directlyAffectedApps(changedPaths, txn, logger) }
    queue.addAll(affected)
    queuedSet.addAll(affected)
    requireBottomUp(cancel)
  }


  /**
   * Require the result of an observable function application in a top-down manner.
   */
  override fun <I : In, O : Out> require(app: FuncApp<I, O>, cancel: Cancelled): ExecRes<O> {
    return requireTopDown(app, cancel)
  }


  /**
   * Require the result of an observable function application in a topdown manner.
   */
  internal open fun <I : In, O : Out> requireTopDown(app: FuncApp<I, O>, cancel: Cancelled): ExecRes<O> {
    cancel.throwIfCancelled()

    try {
      val resOrData = shared.topdownPrelude(app)
      if(resOrData.res != null) {
        return resOrData.res
      }
      val data = resOrData.data

      // Check if re-execution is necessary.
      if(data == null) {
        // No cached or stored output was found: rebuild
        val reason = NoResultReason()
        val execData = exec(app, reason, cancel, true)
        val res = ExecRes(execData.output.cast<O>(), reason)
        logger.requireTopDownEnd(app, res)
        return res
      }
      val (output, callReqs, pathReqs, pathGens) = data

      // Check for inconsistencies and re-execute when found.
      run {
        // Internal consistency: transient output consistency
        /*
        Required for transient outputs. When a function application has a transient output, its output cannot be persisted
        and instead can only be stored in memory. After a restart of the JVM, this memory must be restored by re-executing
        the function application. This check ensures that this happens.
        */
        val reason = output.isTransientInconsistent()
        if(reason != null) {
          val execData = exec(app, reason, cancel)
          val res = ExecRes(execData.output.cast<O>(), reason)
          logger.requireTopDownEnd(app, res)
          return res
        }
      }

      // Total consistency: call requirements
      /*
      Required for checking if all required function calls are consistent.
      */
      for(callReq in callReqs) {
        val callReqOutput = require(callReq.callee, cancel).output
        logger.checkCallReqStart(app, callReq)
        val reason = callReq.checkConsistency(callReqOutput)
        logger.checkCallReqEnd(app, callReq, reason)
        if(reason != null) {
          val execData = exec(app, reason, cancel)
          val res = ExecRes(execData.output.cast<O>(), reason)
          logger.requireTopDownEnd(app, res)
          return res
        }
      }

      // Internal consistency: path requirements
      /*
      Required for checking if all required paths are consistent.
      */
      for(pathReq in pathReqs) {
        logger.checkPathReqStart(app, pathReq)
        val reason = pathReq.checkConsistency()
        if(reason != null) {
          // If a required file is outdated (i.e., its stamp changed): rebuild
          logger.checkPathReqEnd(app, pathReq, reason)
          val execData = exec(app, reason, cancel)
          val res = ExecRes(execData.output.cast<O>(), reason)
          logger.requireTopDownEnd(app, res)
          return res
        } else {
          logger.checkPathReqEnd(app, pathReq, null)
        }
      }

      // Internal consistency: path generates
      /*
      Required for checking if all generated paths are consistent.

      Also, required for overlapping generated paths. When two function applications generate the same path, and those
      function applications overlap (meaning they are allowed to both generate the same path), we must ensure that the
      path is generated by the correct function application. This check ensures that by triggering re-execution of the
      function application if one of its generated paths is inconsistent.
      */
      for(pathGen in pathGens) {
        logger.checkPathGenStart(app, pathGen)
        val reason = pathGen.checkConsistency()
        if(reason != null) {
          // If a generated file is outdated (i.e., its stamp changed): rebuild
          logger.checkPathGenEnd(app, pathGen, reason)
          val execData = exec(app, reason, cancel)
          val res = ExecRes(execData.output.cast<O>(), reason)
          logger.requireTopDownEnd(app, res)
          return res
        } else {
          logger.checkPathGenEnd(app, pathGen, null)
        }
      }

      // No inconsistencies found
      // Validate well-formedness of the dependency graph
      store.readTxn().use { layer.validatePostWrite(app, data, this, it) }
      // Cache and mark as visited
      cache[app] = data
      visited[app] = data
      // Notify observer, if any.
      val observer = observers[app]
      if(observer != null) {
        logger.invokeObserverStart(observer, app, output)
        observer.invoke(output)
        logger.invokeObserverEnd(observer, app, output)
      }
      // Reuse existing result
      val res = ExecRes(output)
      logger.requireTopDownEnd(app, res)
      return res
    } finally {
      layer.requireTopDownEnd(app)
    }
  }

  internal open fun requireBottomUp(cancel: Cancelled) {
    logger.trace("Requiring bottom-up: $queue")
    while(queue.isNotEmpty()) {
      val callee = queue.poll()
      logger.trace("Popped element off queue: ${callee.toShortString(200)}")
      queuedSet.remove(callee)
      val calleeData = exec(callee, InvalidatedExecReason(), cancel)
      logger.trace("Finding out-of-date callers of ${callee.toShortString(200)}")
      val callers = store.readTxn().use { txn -> txn.callersOf(callee) }
      @Suppress("LoopToCallChain")
      for(caller in callers) {
        logger.trace(" * caller: ${caller.toShortString(200)}")
        // OPTO: prevent creating a read transaction twice? cannot create encompassing txn because queue insertion requires read transaction for sorting.
        val callReqs = store.readTxn().use { txn -> txn.callReqs(caller) }
        logger.trace("   * call requirements:")
        for(callReq in callReqs) {
          logger.trace("     * $callReq")
        }
        val relevantCallReqs = callReqs.filter { it.equalsOrOverlaps(callee, this) }
        logger.trace("   * relevant call requirements:")
        for(callReq in relevantCallReqs) {
          logger.trace("     * $callReq")
        }
        val consistent = relevantCallReqs.all { it.isConsistent(calleeData.output) }
        if(consistent) {
          logger.trace("   * is up-to-date")
        } else {
          logger.trace("   * is OUT-OF-DATE")
          if(!queuedSet.contains(caller)) {
            queue.add(caller)
            queuedSet.add(caller)
          }
        }
      }
    }
  }


  internal open fun <I : In, O : Out> exec(app: FuncApp<I, O>, reason: ExecReason, cancel: Cancelled, useCache: Boolean = false): UFuncAppData {
    return shared.exec(app, reason, cancel, useCache) { appL, cancelL -> this.execInternal(appL, cancelL) }
  }

  internal open fun <I : In, O : Out> execInternal(app: FuncApp<I, O>, cancel: Cancelled): UFuncAppData {
    return shared.execInternal(app, cancel, this, this) { txn, data ->
      // Notify observer, if any.
      val observer = observers[app]
      if(observer != null) {
        val output = data.output
        logger.invokeObserverStart(observer, app, output)
        observer.invoke(output)
        logger.invokeObserverEnd(observer, app, output)
      }

      if(hybrid) {
        // Schedule bottom-up execution for function applications affected by generated files.
        val genPaths = data.pathGens.map { it.path }
        logger.trace("Checking which function applications are affected by generated paths: $genPaths")
        val affected = directlyAffectedApps(genPaths, txn, logger)
        queue.addAll(affected)
        queuedSet.addAll(affected)
      }
    }
  }
}
