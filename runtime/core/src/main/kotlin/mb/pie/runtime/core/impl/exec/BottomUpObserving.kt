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
  override fun <I : In, O : Out> requireTopDown(app: FuncApp<I, O>, cancel: Cancelled): O {
    dirty.nextRound()
    val exec = exec()
    return exec.requireTopDownInitial(app, cancel).output
  }

  @Throws(ExecException::class, InterruptedException::class)
  override fun requireBottomUp(changedPaths: List<PPath>, cancel: Cancelled) {
    dirty.nextRound()
    val exec = exec()
    exec.requireBottomUpInitial(changedPaths, cancel)
  }

  override fun <I : In, O : Out> hasBeenRequired(app: FuncApp<I, O>): Boolean {
    return (cache[app] ?: store.readTxn().use { it.output(app) }) != null
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
  private val visited = mutableMapOf<UFuncApp, UFuncAppData>()
  private val generatorOfLocal = mutableMapOf<PPath, UFuncApp>()
  private val shared = TopDownExecShared(store, cache, share, layer, logger, visited, generatorOfLocal)


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
    val directlyAffected = store.readTxn().use { txn ->
      val directlyAffected = directlyAffectedApps(changedPaths, txn, logger)

      if(variant == DirtyFlagging) {
        // Set affected to dirty
        logger.trace("Dirty flagging")
        for(app in directlyAffected) {
          logger.trace("* dirty: ${app.toShortString(200)}")
          dirty.setDirty(app)
        }

        // Propagate dirty flags
        logger.trace("Dirty flag propagation")
        val todo = ArrayDeque(directlyAffected)
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
      directlyAffected
    }

    for(app in directlyAffected) {
      logger.requireBottomUpInitialStart(app)
      val info = requireBottomUp(app, null, null, cancel)
      logger.requireBottomUpInitialEnd(app, info)
    }
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
      // TODO: set function application as observed

      val resOrData = shared.topdownPrelude(app)
      if(resOrData.res != null) {
        return resOrData.res
      }
      val data = resOrData.data

      // Check if re-execution is necessary.
      if(data == null) {
        // No cached or stored output was found: rebuild
        val res = exec(app, NoResultReason(), cancel, true)
        logger.requireTopDownEnd(app, res)
        return res
      }
      val (output, _, _, pathGens) = data

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
          val res = exec(app, reason, cancel)
          logger.requireTopDownEnd(app, res)
          return res
        }
      }

      if(variant == DirtyFlagging) {
        // Internal consistency: dirty flagged.
        if(dirty.isDirty(app)) {
          val info = exec(app, DirtyFlaggedReason(), cancel)
          logger.requireTopDownEnd(app, info)
          return info
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
        logger.checkPathGenStart(app, pathGen)
        val reason = pathGen.checkConsistency()
        if(reason != null) {
          // If a generated file is outdated (i.e., its stamp changed): rebuild
          logger.checkPathGenEnd(app, pathGen, reason)
          val res = exec(app, reason, cancel)
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

  /**
   * Require the result of a new observable function application in a bottom-up manner.
   */
  internal open fun <I : In, O : Out> requireBottomUp(caller: FuncApp<I, O>, callee: UFuncApp?, calleeOutput: Out?, cancel: Cancelled): ExecRes<O>? {
    cancel.throwIfCancelled()
    logger.requireBottomUpStart(caller)
    val res = bottomUpResult(caller, callee, calleeOutput, cancel)
    logger.trace("Recursing bottom-up over inconsistent callers of ${caller.toShortString(100)}")
    for(callerOfCaller in store.readTxn().use { txn -> txn.callersOf(caller) }) {
      cancel.throwIfCancelled()
      logger.trace("* caller: ${callerOfCaller.toShortString(100)}")
      requireBottomUp(callerOfCaller, caller, res.output, cancel)
    }
    logger.requireBottomUpEnd(caller, res)
    return res
  }

  internal open fun <I : In, O : Out> bottomUpResult(caller: FuncApp<I, O>, callee: UFuncApp?, calleeOutput: Out?, cancel: Cancelled): ExecRes<O> {
    if(callee == null || calleeOutput == null) {
      // When callee is null, caller is directly affected: execute.
      return exec(caller, InvalidatedExecReason(), cancel)
    }

    when(variant) {
      Naive -> {
        // Naive variant cannot skip execution when already visited, it would be unsound.
      }
      DirtyFlagging -> {
        val visitedData = visited[caller]
        if(visitedData != null && !dirty.isDirty(caller)) {
          // If non-dirty function application was already visited: skip execution.
          return ExecRes(visitedData.output.cast())
        }
      }
      TopologicalSort -> {
        val visitedData = visited[caller]
        if(visitedData != null) {
          // If function application was already visited: skip execution.
          return ExecRes(visitedData.output.cast())
        }
      }
    }

    val callerData = shared.existingData(caller)
    @Suppress("FoldInitializerAndIfToElvis")
    if(callerData == null) {
      // When there is no existing result: execute.
      /*
      Required for cancellation and exception support, which model early termination. When execution is terminated
      early, dependencies may have been stored in the store, whereas the result of execution has not.
      */
      return exec(caller, NoResultReason(), cancel)
    }

    val transientInconsistencyReason = callerData.output.isTransientInconsistent()
    if(transientInconsistencyReason != null) {
      // When output is internally inconsistent: execute.
      /*
      Required for transient outputs. When a function application has a transient output, its output cannot be persisted
      and instead can only be stored in memory. After a restart of the JVM, this memory must be restored by re-executing
      the function application. This check ensures that this happens.
      */
      return exec(caller, transientInconsistencyReason, cancel)
    }

    logger.trace("Checking if call from ${caller.toShortString(100)} to ${callee.toShortString(100)} is consistent")
    // Caller's call requirements are available, callee and calleeOutput are not null.
    if(!callerData.callReqs.filter { it.callee == callee }.all { it.isConsistent(calleeOutput) }) {
      // If a call requirement from caller to callee is inconsistent: execute
      return exec(caller, InvalidatedExecReason(), cancel)
    }

    // Skip execution
    // TODO: Validate well-formedness of the dependency graph?
    // Cache and mark as visited
    // TODO: is this valid for naive execution?
    cache[caller] = callerData
    visited[caller] = callerData
    // Notify observer, if any.
    val observer = observers[caller]
    if(observer != null) {
      val output = callerData.output
      logger.invokeObserverStart(observer, caller, output)
      observer.invoke(output)
      logger.invokeObserverEnd(observer, caller, output)
    }
    // Reuse existing result
    return ExecRes(callerData.output)
  }

  /**
   * Executes [app] and returns it result. Tries to share [app]'s execution with other threads using [share].
   */
  internal open fun <I : In, O : Out> exec(app: FuncApp<I, O>, reason: ExecReason, cancel: Cancelled, useCache: Boolean = false): ExecRes<O> {
    return shared.exec(app, reason, cancel, useCache, this::execInternal)
  }

  /**
   * Performs the actual execution of [app] and returns it result.
   */
  internal open fun <I : In, O : Out> execInternal(app: FuncApp<I, O>, cancel: Cancelled): O {
    return shared.execInternal(app, cancel, this, this) { _, data ->
      // Notify observer, if any.
      val observer = observers[app]
      if(observer != null) {
        val output = data.output
        logger.invokeObserverStart(observer, app, output)
        observer.invoke(output)
        logger.invokeObserverEnd(observer, app, output)
      }
      // TODO: mark observed, not-dirty
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
