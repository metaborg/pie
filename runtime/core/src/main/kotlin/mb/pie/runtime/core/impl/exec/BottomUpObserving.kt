package mb.pie.runtime.core.impl.exec

import com.google.inject.Inject
import com.google.inject.Provider
import com.google.inject.assistedinject.Assisted
import mb.pie.runtime.core.*
import mb.pie.runtime.core.exec.*
import mb.pie.runtime.core.exec.BottomUpObservingExecutorFactory.Variant.*
import mb.pie.runtime.core.impl.*
import mb.util.async.Cancelled
import mb.util.async.NullCancelled
import mb.vfs.path.PPath
import java.util.*
import java.util.concurrent.ConcurrentHashMap


class BottomUpObservingExecutorImpl @Inject constructor(
  @Assisted private val store: Store,
  @Assisted private val cache: Cache,
  @Assisted private val variant: BottomUpObservingExecutorFactory.Variant,
  private val share: Share,
  private val layer: Provider<Layer>,
  private val logger: Provider<Logger>,
  private val funcs: MutableMap<String, UFunc>
) : BottomUpObservingExecutor {
  private val keyToApp = ConcurrentHashMap<Any, UFuncApp>()
  private val appToObs = ConcurrentHashMap<UFuncApp, FuncAppObserver>()
  private val dirty = DirtyState()


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
    dirty.nextRound()
    val exec = exec()
    return exec.requireTopDownInitial(app, cancel)
  }

  @Throws(ExecException::class, InterruptedException::class)
  override fun requireBottomUp(changedPaths: List<PPath>, cancel: Cancelled) {
    dirty.nextRound()
    val exec = exec()
    exec.requireBottomUpInitial(changedPaths, cancel)
  }

  override fun <I : In, O : Out> hasBeenRequired(app: FuncApp<I, O>): Boolean {
    return (cache[app] ?: store.readTxn().use { it.resultOf(app) }) != null
  }


  override fun garbageCollect() {
    // TODO: Clean up all data for non-observed function applications.
  }


  fun exec(): BottomUpObservingExec {
    return BottomUpObservingExec(store, cache, share, layer.get(), logger.get(), funcs, appToObs, variant, dirty)
  }


  override fun dropStore() {
    store.writeTxn().use { it.drop() }
    store.sync()
  }

  override fun dropCache() {
    cache.drop()
  }
}

class DirtyState {
  private val dirty: MutableMap<UFuncApp, Int> = mutableMapOf()
  private var roundMarker: Int = 0


  fun nextRound() = ++roundMarker

  fun isDirty(app: UFuncApp): Boolean {
    val mark = dirty[app] ?: return false
    return mark >= roundMarker
  }

  fun setDirty(app: UFuncApp) {
    dirty[app] = roundMarker
  }
}

open class BottomUpObservingExec(
  private val store: Store,
  private val cache: Cache,
  private val share: Share,
  private val layer: Layer,
  private val logger: Logger,
  private val funcs: Map<String, UFunc>,
  private val observers: Map<UFuncApp, FuncAppObserver>,
  private val variant: BottomUpObservingExecutorFactory.Variant,
  private val dirty: DirtyState
) : Exec, Funcs by FuncsImpl(funcs) {
  private val visited = mutableMapOf<UFuncApp, UExecRes>()


  /**
   * Require the result of an observable function application in a top-down manner.
   */
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
   * Execute function applications affected by a changed path in a bottom-up manner.
   */
  fun requireBottomUpInitial(changedPaths: List<PPath>, cancel: Cancelled) {
    if(changedPaths.isEmpty()) return

    // Find all function applications that are affected by changed paths.
    val affected = HashSet<UFuncApp>()
    logger.trace("Finding affected function applications for changed paths")
    store.readTxn().use { txn ->
      for(changedPath in changedPaths) {
        logger.trace("* changed path: $changedPath")
        // Check function applications that require the changed path.
        val requirees = txn.requireesOf(changedPath)
        for(requiree in requirees) {
          logger.trace("  - required by: ${requiree.toShortString(200)}")
          if(!pathIsConsistent(requiree, changedPath, txn, { path, res -> res.pathReqs.firstOrNull { path == it.path } })) {
            affected.add(requiree)
          }
        }
        // Check function applications that generate the changed path.
        val generatorOf = txn.generatorOf(changedPath)
        if(generatorOf != null) {
          logger.trace("  - generated by: ${generatorOf.toShortString(200)}")
          if(!pathIsConsistent(generatorOf, changedPath, txn, { path, res -> res.gens.firstOrNull { path == it.path } })) {
            affected.add(generatorOf)
          }
        }
      }

      if(variant == DirtyFlagging) {
        // Set affected to dirty
        logger.trace("Dirty flagging")
        for(app in affected) {
          logger.trace("* dirty: ${app.toShortString(200)}")
          dirty.setDirty(app)
        }

        // Propagate dirty flags
        logger.trace("Dirty flag propagation")
        val todo = ArrayDeque(affected)
        val seen = HashSet<UFuncApp>()
        while(!todo.isEmpty()) {
          val app = todo.pop()
          if(!seen.contains(app)) {
            logger.trace("* dirty: ${app.toShortString(200)}")
            // Optimisation: check if app was already dirty flagged. Don't do transitive flagging if so. Be sure to add to seen.
            dirty.setDirty(app)
            seen.add(app)
            val callersOf = txn.callersOf(app)
            callersOf.forEach { logger.trace("  - called by: ${it.toShortString(200)}") }
            todo += callersOf
          }
        }
      }
    }

    for(app in affected) {
      logger.requireBottomUpInitialStart(app)
      val info = requireBottomUp(app, null, null, cancel)
      logger.requireBottomUpInitialEnd(app, info)
    }
  }


  /**
   * Require the result of an observable function application in a top-down manner.
   */
  override fun <I : In, O : Out> require(app: FuncApp<I, O>, cancel: Cancelled): ExecInfo<I, O> {
    return requireTopDown(app, cancel)
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

      // Get cached or stored result.
      val existingResult = existingResult(app)

      // Check if re-execution is necessary.
      if(existingResult == null) {
        // No cached or stored result was found: rebuild
        val info = exec(app, NoResultReason(), cancel, true)
        logger.requireTopDownEnd(app, info)
        return info
      }

      // Check for inconsistencies and re-execute when found.
      if(variant == DirtyFlagging) {
        // Internal consistency: dirty flagged.
        if(dirty.isDirty(app)) {
          val info = exec(app, DirtyFlaggedReason(), cancel)
          logger.requireTopDownEnd(app, info)
          return info
        }
      }

      // Internal consistency: output consistency
      /*
      Required for transient outputs. When a function application has a transient output, its output cannot be persisted
      and instead can only be stored in memory. After a restart of the JVM, this memory must be restored by re-executing
      the function application. This check ensures that this happens.
      */
      run {
        val inconsistencyReason = existingResult.internalInconsistencyReason
        if(inconsistencyReason != null) {
          return exec(app, inconsistencyReason, cancel)
        }
      }

      // Internal consistency: generated files
      /*
      Required for overlapping generated paths. When two function applications generate the same path, and those
      function applications overlap (meaning they are allowed to both generate the same path), we must ensure that the
      path is generated by the correct function application. This check ensures that by triggering re-execution of the
      function application if one of its generated paths is inconsistent.
      */
      for(gen in existingResult.gens) {
        val (genPath, stamp) = gen
        logger.checkGenStart(app, gen)
        val newStamp = stamp.stamper.stamp(genPath)
        if(stamp != newStamp) {
          // If a generated file is outdated (i.e., its stamp changed): rebuild
          val reason = InconsistentGenPath(existingResult, gen, newStamp)
          logger.checkGenEnd(app, gen, reason)
          val info = exec(app, reason, cancel)
          logger.requireTopDownEnd(app, info)
          return info
        } else {
          logger.checkGenEnd(app, gen, null)
        }
      }

      // No inconsistencies found
      // Validate well-formedness of the dependency graph
      store.readTxn().use { layer.validate(app, existingResult, this, it) }
      // Cache and mark as visited
      cache[app] = existingResult
      visited[app] = existingResult
      // Notify observer, if any.
      val observer = observers[app]
      if(observer != null) {
        val output = existingResult.output
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
  internal open fun <I : In, O : Out> requireBottomUp(caller: FuncApp<I, O>, callee: UFuncApp?, calleeRes: UExecRes?, cancel: Cancelled): ExecInfo<I, O>? {
    cancel.throwIfCancelled()
    logger.requireBottomUpStart(caller)
    val info = bottomUpResult(caller, callee, calleeRes, cancel)
    logger.trace("Recursing bottom-up over inconsistent callers of ${caller.toShortString(100)}")
    for(callerOfCaller in store.readTxn().use { txn -> txn.callersOf(caller) }) {
      cancel.throwIfCancelled()
      logger.trace("* caller: ${callerOfCaller.toShortString(100)}")
      requireBottomUp(callerOfCaller, caller, info.result, cancel)
    }
    logger.requireBottomUpEnd(caller, info)
    return info
  }

  internal open fun <I : In, O : Out> bottomUpResult(caller: FuncApp<I, O>, callee: UFuncApp?, calleeRes: UExecRes?, cancel: Cancelled): ExecInfo<I, O> {
    if(callee == null || calleeRes == null) {
      // When callee is null, caller is directly affected: execute.
      return exec(caller, InvalidatedExecReason(), cancel)
    }

    when(variant) {
      Naive -> {
        // Naive variant cannot skip execution when already visited, it would be unsound.
      }
      DirtyFlagging -> {
        val visitedRes = visited[caller]
        if(visitedRes != null && !dirty.isDirty(caller)) {
          // If non-dirty function application was already visited: skip execution.
          return ExecInfo(visitedRes.cast<I, O>(), null)
        }
      }
      TopologicalSort -> {
        val visitedRes = visited[caller]
        if(visitedRes != null) {
          // If function application was already visited: skip execution.
          return ExecInfo(visitedRes.cast<I, O>(), null)
        }
      }
    }

    val existingRes = existingResult(caller)
    @Suppress("FoldInitializerAndIfToElvis")
    if(existingRes == null) {
      // When there is no existing result: execute.
      /*
      Required for cancellation and exception support, which model early termination. When execution is terminated
      early, dependencies may have been stored in the store, whereas the result of execution has not.
      */
      return exec(caller, NoResultReason(), cancel)
    }

    val internalInconsistencyReason = existingRes.internalInconsistencyReason
    if(internalInconsistencyReason != null) {
      // When output is internally inconsistent: execute.
      /*
      Required for transient outputs. When a function application has a transient output, its output cannot be persisted
      and instead can only be stored in memory. After a restart of the JVM, this memory must be restored by re-executing
      the function application. This check ensures that this happens.
      */
      return exec(caller, internalInconsistencyReason, cancel)
    }

    logger.trace("Checking if call from ${caller.toShortString(100)} to ${callee.toShortString(100)} is consistent")
    // Caller result is available, callee and calleeRes are not null (first if check)
    if(!store.readTxn().use { txn -> callIsConsistent(caller, existingRes, callee, calleeRes, txn) }) {
      // If a call requirement from caller to callee is inconsistent: execute
      return exec(caller, InvalidatedExecReason(), cancel)
    }

    // Skip execution
    // TODO: Validate well-formedness of the dependency graph?
    // Cache and mark as visited
    // TODO: is this valid for naive execution?
    cache[caller] = existingRes
    visited[caller] = existingRes
    // Notify observer, if any.
    val observer = observers[caller]
    if(observer != null) {
      val output = existingRes.output
      logger.invokeObserverStart(observer, caller, output)
      observer.invoke(output)
      logger.invokeObserverEnd(observer, caller, output)
    }
    // Reuse existing result
    return ExecInfo(existingRes)
  }


  internal open fun <I : In, O : Out> existingResult(app: FuncApp<I, O>): ExecRes<I, O>? {
    // Check cache for result of function application.
    logger.checkCachedStart(app)
    val cachedResult = cache[app]
    logger.checkCachedEnd(app, cachedResult)

    // Check store for result of function application.
    return if(cachedResult != null) {
      cachedResult
    } else {
      logger.checkStoredStart(app)
      val result = store.readTxn().use { it.resultOf(app) }
      logger.checkStoredEnd(app, result)
      result
    }?.cast<I, O>()
  }

  /**
   * @return `true` when [requiree]'s path requirement to [path] is consistent, `false` otherwise.
   */
  internal open fun pathIsConsistent(requiree: UFuncApp, path: PPath, txn: StoreReadTxn, checkerGenFunc: (PPath, UExecRes) -> ConsistencyChecker?): Boolean {
    val result =
      cache[requiree] ?: txn.resultOf(requiree) ?: run {
        // Can occur when an execution is cancelled and its result is not stored. Log and assume that it is changed.
        logger.trace("    - no result: ${requiree.toShortString(200)}")
        return false
      }

    val consistencyChecker = checkerGenFunc(path, result)
    if(consistencyChecker == null) {
      // Should not happen. Log error and assume change.
      logger.error("    - could not find consistency checker for path $path in ${result.toShortString(200)}")
      return false
    }

    if(!consistencyChecker.isConsistent()) {
      logger.trace("    - not consistent: $consistencyChecker")
      return false
    }

    logger.trace("    - consistent: $consistencyChecker")

    return true
  }

  /**
   * @return `true` when [caller]'s call requirement to [callee] is consistent, `false` otherwise.
   */
  internal open fun callIsConsistent(caller: UFuncApp, callerRes: UExecRes, callee: UFuncApp, calleeRes: UExecRes, txn: StoreReadTxn): Boolean {
    // caller and callee results are assumed to be internally consistent.

    val callReqs = callerRes.callReqs(callee, this)
    if(callReqs.isEmpty()) {
      // Should not happen. Log error and assume change.
      logger.error("  - could not find call requirement for callee ${callee.toShortString(100)} by caller ${callerRes.toShortString(100)}")
      return false
    }

    var allConsistent = true
    for(callReq in callReqs) {
      val consistent = callReq.isConsistent(calleeRes)
      if(consistent) {
        logger.trace("  - consistent: $callReq")
      } else {
        logger.trace("  - not consistent: $callReq")
      }
      allConsistent = allConsistent && consistent
    }
    return allConsistent
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

      // Cache and mark as visited
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
