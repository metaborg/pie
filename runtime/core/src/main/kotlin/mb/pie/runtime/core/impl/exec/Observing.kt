package mb.pie.runtime.core.impl.exec

import com.google.inject.Inject
import com.google.inject.Provider
import com.google.inject.assistedinject.Assisted
import mb.pie.runtime.core.*
import mb.pie.runtime.core.exec.FuncAppObserver
import mb.pie.runtime.core.exec.ObservingExecutor
import mb.pie.runtime.core.impl.*
import mb.util.async.Cancelled
import mb.util.async.NullCancelled
import mb.vfs.path.PPath
import java.util.*
import java.util.concurrent.ConcurrentHashMap


class ObservingExecutorImpl @Inject constructor(
  @Assisted private val store: Store,
  @Assisted private val cache: Cache,
  private val share: Share,
  private val layer: Provider<Layer>,
  private val logger: Provider<Logger>,
  mbLogger: mb.log.Logger,
  private val funcs: MutableMap<String, UFunc>
) : ObservingExecutor {
  private val mbLogger = mbLogger.forContext(ObservingExecutorImpl::class.java)

  private val keyToApp = ConcurrentHashMap<Any, UFuncApp>()
  private val appToObs = ConcurrentHashMap<UFuncApp, FuncAppObserver>()
//  private val observed = ConcurrentHashMap.newKeySet<UFuncApp>()
//  private val dirty = ConcurrentHashMap.newKeySet<UFuncApp>()
//  private val lock = ReentrantReadWriteLock()


  override fun setObserver(key: Any, app: UFuncApp, observer: FuncAppObserver) {
    /* TODO:
    If function application was not executed before, execute it and set all (transitive) function applications to observed, notify observer of the result.

    If function application was executed before, go topdown over all (transitive) function applications, and set them to observed.
    Stop traversing dependency tree when an already observed node is encountered.
    */

    val existingApp = keyToApp[key]
    if(existingApp != null) {
      appToObs.remove(existingApp)
    }
    keyToApp[key] = app
    appToObs[app] = observer
  }

  override fun removeObserver(key: Any) {
    /* TODO:
    If function application was not executed before, set function as not observed.

    If function application was executed before, go topdown over all (transitive) function applications, and set them to not-observed.
    Stop traversing dependency tree when an already not-observed node is encountered. TODO: that cannot happen?
    */

    val app = keyToApp[key]
    if(app != null) {
      appToObs.remove(app)
    }
    keyToApp.remove(key)
  }


  @Throws(ExecException::class, InterruptedException::class)
  override fun <I : In, O : Out> requireTopDown(app: FuncApp<I, O>, cancel: Cancelled): ExecInfo<I, O> {
    val exec = exec()
    return exec.requireTopDownInitial(app, cancel)
  }

  @Throws(ExecException::class, InterruptedException::class)
  override fun requireBottomUp(changedPaths: List<PPath>, cancel: Cancelled) {
    val exec = exec()
    exec.pathsChanged(changedPaths, cancel)
  }

  override fun <I : In, O : Out> hasBeenRequired(app: FuncApp<I, O>): Boolean {
    return (cache[app] ?: store.readTxn().use { it.resultOf(app) }) != null;
  }


  override fun garbageCollect() {
    // TODO: Clean up all data for non-observed function applications.
  }


  fun exec(): ObservingExec {
    return ObservingExec(store, cache, share, layer.get(), logger.get(), mbLogger, funcs, appToObs)
  }


  override fun dropStore() {
    store.writeTxn().use { it.drop() }
    store.sync()
  }

  override fun dropCache() {
    cache.drop()
  }
}

open class ObservingExec(
  private val store: Store,
  private val cache: Cache,
  private val share: Share,
  private val layer: Layer,
  private val logger: Logger,
  private val mbLogger: mb.log.Logger,
  private val funcs: Map<String, UFunc>,
  private val observers: Map<UFuncApp, FuncAppObserver>
) : Exec, Funcs by FuncsImpl(funcs) {
  private val visited = mutableMapOf<UFuncApp, UExecRes>()


  fun <I : In, O : Out> requireTopDownInitial(app: FuncApp<I, O>, cancel: Cancelled = NullCancelled()): ExecInfo<I, O> {
    try {
      logger.requireTopDownInitialStart(app)
      val info = require(app, cancel)
      logger.requireTopDownInitialEnd(app, info)
      return info
    } finally {
      store.sync()
    }
  }

  /**
   * Require the result of an observable function application in a top-down manner.
   */
  override fun <I : In, O : Out> require(app: FuncApp<I, O>, cancel: Cancelled): ExecInfo<I, O> {
    return requireTopDown(app, cancel)
  }

  /**
   * Execute function applications affected by a changed path in a bottom-up manner.
   */
  fun pathsChanged(changedPaths: List<PPath>, cancel: Cancelled) {
    if(changedPaths.isEmpty()) return

    // Find all function applications that are affected by changed paths.
    val affected = HashSet<UFuncApp>()
    mbLogger.trace("Initial dirty flagging")
    store.readTxn().use { txn ->
      for(changedPath in changedPaths) {
        mbLogger.trace("  changed: $changedPath")
        // Check function applications that require the changed path.
        val requirees = txn.requireesOf(changedPath)
        for(requiree in requirees) {
          mbLogger.trace("  required by: ${requiree.toShortString(200)}")
          if(!pathIsConsistent(requiree, changedPath, txn, { path, res -> res.pathReqs.firstOrNull { path == it.path } })) {
            affected.add(requiree)
          }
        }
        // Check function applications that generate the changed path.
        val generatorOf = txn.generatorOf(changedPath)
        if(generatorOf != null) {
          mbLogger.trace("  generated by: ${generatorOf.toShortString(200)}")
          if(!pathIsConsistent(generatorOf, changedPath, txn, { path, res -> res.gens.firstOrNull { path == it.path } })) {
            affected.add(generatorOf)
          }
        }
      }
    }
    for(app in affected) {
      // TODO: give proper execution reason
      logger.requireBottomUpInitialStart(app)
      val info = requireBottomUp(app, InvalidatedExecReason(), cancel)
      logger.requireBottomUpInitialEnd(app, info)
    }
  }


  /**
   * Require the result of an observable function application in a topdown manner.
   */
  internal open fun <I : In, O : Out> requireTopDown(app: FuncApp<I, O>, cancel: Cancelled): ExecInfo<I, O> {
    cancel.throwIfCancelled()

    try {
      layer.requireTopDownStart(app)
      logger.requireTopDownStart(app)

      // Return result immediately if function application was already visited this execution.
      logger.checkVisitedStart(app)
      val visitedResult = visited[app]?.cast<I, O>()
      if(visitedResult != null) {
        // Existing result is known to be consistent this execution: reuse
        logger.checkVisitedEnd(app, visitedResult)
        val info = ExecInfo(visitedResult)
        logger.requireTopDownEnd(app, info)
        return info
      }
      logger.checkVisitedEnd(app, null)

      // TODO: set function application as observed

      // Check cache for result of function application.
      logger.checkCachedStart(app)
      val cachedResult = cache[app]
      logger.checkCachedEnd(app, cachedResult)

      // Check store for result of function application.
      val existingResult = if(cachedResult != null) {
        cachedResult
      } else {
        logger.checkStoredStart(app)
        val result = store.readTxn().use { it.resultOf(app) }
        logger.checkStoredEnd(app, result)
        result
      }?.cast<I, O>()

      // Check if re-execution is necessary.
      if(existingResult == null) {
        // No cached or stored result was found: rebuild
        val info = exec(app, NoResultReason(), cancel, true)
        logger.requireTopDownEnd(app, info)
        return info
      }

      // Check for inconsistencies and re-execute when found.
      // Internal consistency: output consistency
      run {
        val inconsistencyReason = existingResult.internalInconsistencyReason
        if(inconsistencyReason != null) {
          return exec(app, inconsistencyReason, cancel)
        }
      }

      // TODO: internal consistency: dirty flagged.

      // No inconsistencies found
      // Validate well-formedness of the dependency graph
      store.readTxn().use { layer.validate(app, existingResult, this, it) }
      // Cache and mark as visited
      cache[app] = existingResult
      visited[app] = existingResult
      // Notify observer, if any.
      val observer = observers[app]
      if(observer != null) {
        val output = existingResult.output;
        logger.invokeObserverStart(observer, app, output)
        observer.invoke(output)
        logger.invokeObserverEnd(observer, app, output)
      }
      // Reuse existing result
      val info = ExecInfo(existingResult)
      logger.requireTopDownEnd(app, info)
      return info
    } finally {
      layer.requireTopDownEnd(app)
    }
  }

  /**
   * Require the result of a new observable function application in a bottom-up manner.
   */
  internal open fun <I : In, O : Out> requireBottomUp(app: FuncApp<I, O>, reason: ExecReason, cancel: Cancelled): ExecInfo<I, O>? {
    cancel.throwIfCancelled()

    try {
      //layer.requireTopDownStart(app)
      logger.requireBottomUpStart(app)

      // UNDONE: does not work, skips too many executions.
      // Stop if function application was already visited this execution.
      //    if(visited.contains(app)) {
      //      return
      //    }

      // TODO: if function application is not observed, mark as dirty and stop.

      // Re-execute.
      val res = exec(app, reason, cancel).result

      // Require all inconsistent callers of the current function application in a bottom-up manner.
      val inconsistentCallers = store.readTxn().use { txn ->
        txn.callersOf(app).filterNot { callIsConsistent(it, app, res, txn) }
      }
      for(inconsistentCaller in inconsistentCallers) {
        // TODO: give proper execution reason
        requireBottomUp(inconsistentCaller, InvalidatedExecReason(), cancel)
      }

      val info = ExecInfo(res)
      logger.requireBottomUpEnd(app, info)
      return info
    } finally {
      //layer.requireTopDownEnd(app)
    }
  }


  /**
   * @return `true` when [requiree]'s path requirement to [path] is consistent, `false` otherwise.
   */
  internal open fun pathIsConsistent(requiree: UFuncApp, path: PPath, txn: StoreReadTxn, checkerGenFunc: (PPath, UExecRes) -> ConsistencyChecker?): Boolean {
    val result =
      cache[requiree] ?: txn.resultOf(requiree) ?: run {
        // Can occur when an execution is cancelled and its result is not stored. Log and assume that it is changed.
        mbLogger.trace("  no result: ${requiree.toShortString(200)}")
        return false
      }

    val consistencyChecker = checkerGenFunc(path, result)
    if(consistencyChecker == null) {
      // Should not happen. Log error and assume change.
      mbLogger.error("Could not find consistency checker for path $path in ${result.toShortString(200)}")
      return false
    }

    if(!consistencyChecker.isConsistent()) {
      mbLogger.trace("  not consistent: $consistencyChecker")
      return false
    }

    return true
  }

  /**
   * @return `true` when [caller]'s call requirement to [callee] is consistent, `false` otherwise.
   */
  internal open fun callIsConsistent(caller: UFuncApp, callee: UFuncApp, calleeRes: UExecRes, txn: StoreReadTxn): Boolean {
    val callerRes =
      cache[caller] ?: txn.resultOf(caller) ?: run {
        // Can occur when an execution is cancelled and its result is not stored. Log and assume that it is not consistent.
        mbLogger.trace("  no result: ${caller.toShortString(200)}")
        return false
      }

    if(!callerRes.isInternallyConsistent) {
      mbLogger.trace("  not internally consistent: ${callerRes.toShortString(200)}")
      return false
    }

    // Omit internal consistency check for calleeRes, it is assumed to be internally consistent.

    val callReqs = callerRes.callReqs(callee, this)
    if(callReqs.isEmpty()) {
      // Should not happen. Log error and assume change.
      mbLogger.error("Could not find call requirement for callee ${callee.toShortString(100)} by caller ${callerRes.toShortString(100)}")
      return false
    }

    if(!callReqs.all { it.isConsistent(calleeRes) }) {
      mbLogger.trace("  not all consistent: $callReqs")
      return false
    }

    return true
  }


  /**
   * Executes [app] and returns it result. Tries to share [app]'s execution with other threads using [share].
   */
  internal open fun <I : In, O : Out> exec(app: FuncApp<I, O>, reason: ExecReason, cancel: Cancelled, useCache: Boolean = false): ExecInfo<I, O> {
    cancel.throwIfCancelled()
    logger.rebuildStart(app, reason)
    val result = if(useCache) {
      share.reuseOrCreate(app, { store.readTxn().use { txn -> txn.resultOf(it)?.cast<I, O>() } }) { this.execInternal(it, cancel) }
    } else {
      share.reuseOrCreate(app) { this.execInternal(it, cancel) }
    }
    logger.rebuildEnd(app, reason, result)
    return ExecInfo(result, reason)
  }

  /**
   * Performs the actual execution of [app] and returns it result.
   */
  internal open fun <I : In, O : Out> execInternal(app: FuncApp<I, O>, cancel: Cancelled): ExecRes<I, O> {
    cancel.throwIfCancelled()

    val (builderId, input) = app
    val builder = getFunc<I, O>(builderId)
    val desc = builder.desc(input)
    val context = ExecContextImpl(this, store, app, cancel)

    try {
      val output = builder.exec(input, context)
      val (reqs, gens) = context.getReqsAndGens()
      val result = ExecRes(builderId, desc, input, output, reqs, gens)

      // Validate well-formedness of the dependency graph
      store.readTxn().use { txn -> layer.validate(app, result, this, txn) }

      // Write to store
      store.writeTxn().use { txn ->
        // Store result
        txn.setResultOf(app, result)
        // Store path dependencies
        context.writePathDepsToStore(txn)
      }

      // Cache and mark as visisted
      cache[app] = result
      visited[app] = result
      // TODO: mark observed, not-dirty

      // Notify observer, if any.
      val observer = observers[app]
      if(observer != null) {
        logger.invokeObserverStart(observer, app, output)
        observer.invoke(output)
        logger.invokeObserverEnd(observer, app, output)
      }

      return result
    } catch(e: InterruptedException) {
      store.writeTxn().use { txn ->
        // Can't write result, since none is produced when execution is interrupted
        // Store path dependencies which have been made so far
        // TODO: is this necessary? They should be re-discovered on re-execution?
        context.writePathDepsToStore(txn)
      }
      throw e
    }
  }
}

class InvalidatedExecReason : ExecReason {
  override fun toString() = "invalidated"


  override fun equals(other: Any?): Boolean {
    if(this === other) return true
    if(other?.javaClass != javaClass) return false
    return true
  }

  override fun hashCode(): Int {
    return 0
  }
}
