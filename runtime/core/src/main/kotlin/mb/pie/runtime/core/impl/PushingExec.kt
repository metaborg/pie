package mb.pie.runtime.core.impl

import com.google.inject.Inject
import com.google.inject.Provider
import com.google.inject.assistedinject.Assisted
import mb.pie.runtime.core.*
import mb.vfs.path.PPath
import java.util.*

class PushingExecutorImpl @Inject constructor(
  private @Assisted val store: Store,
  private @Assisted val cache: Cache,
  private val share: BuildShare,
  private val layer: Provider<Layer>,
  private val logger: Provider<Logger>,
  private val funcs: MutableMap<String, UFunc>
) : PushingExecutor {
  override fun require(obsFuncs: List<AnyObsFunc>, changedPaths: List<PPath>) {
    // Find function applications that are directly marked dirty by changed paths
    val directDirty = HashSet<UFuncApp>()
    store.readTxn().use { txn ->
      for(changedPath in changedPaths) {
        val requiredBy = txn.requiredBy(changedPath)
        directDirty += requiredBy
        val generatedBy = txn.generatedBy(changedPath)
        if(generatedBy != null) {
          directDirty.add(generatedBy)
        }
      }
    }

    // Propagate dirty flags and persist to storage.
    store.writeTxn().use { txn ->
      val todo = ArrayDeque(directDirty)
      while(!todo.isEmpty()) {
        val app = todo.pop()
        txn.setIsDirty(app, true)
        val calledBy = txn.calledBy(app)
        todo += calledBy;
      }
    }

    // Execute observable functions, push result when function was executed.
    val exec = PushingExec(store, cache, share, layer.get(), logger.get(), funcs)
    for((app, changedFunc) in obsFuncs) {
      val info = exec.require(app)
      if(info.wasExecuted) {
        changedFunc(info.result.output)
      }
    }
  }


  override fun dropStore() {
    store.writeTxn().drop()
  }

  override fun dropCache() {
    cache.drop()
  }
}

open class PushingExec(
  private val store: Store,
  private val cache: Cache,
  private val share: BuildShare,
  private val layer: Layer,
  private val logger: Logger,
  private val funcs: Map<String, UFunc>
) : Exec, Funcs by FuncsImpl(funcs) {
  private val consistent = mutableMapOf<UFuncApp, UExecRes>()


  override fun <I : In, O : Out> require(app: FuncApp<I, O>): ExecInfo<I, O> {
    try {
      layer.requireStart(app)
      logger.requireStart(app)

      // Check if function application is already consistent this execution.
      logger.checkConsistentStart(app)
      val consistentResult = consistent[app]?.cast<I, O>()
      if(consistentResult != null) {
        // Existing result is known to be consistent this build: reuse
        logger.checkConsistentEnd(app, consistentResult)
        val info = ExecInfo(consistentResult)
        logger.requireEnd(app, info)
        return info
      }
      logger.checkConsistentEnd(app, null)

      // Check cache for result of build application.
      logger.checkCachedStart(app)
      val cachedResult = cache[app]
      logger.checkCachedEnd(app, cachedResult)

      // Check store for result of build application.
      val existingUntypedResult = if(cachedResult != null) {
        cachedResult
      } else {
        logger.checkStoredStart(app)
        val result = store.readTxn().use { it.resultsIn(app) }
        logger.checkStoredEnd(app, result)
        result
      }

      // Check if rebuild is necessary.
      val existingResult = existingUntypedResult?.cast<I, O>()
      if(existingResult == null) {
        // No cached or stored result was found: rebuild
        val info = exec(app, NoResultReason(), true)
        logger.requireEnd(app, info)
        return info
      }

      // Check if result is internally consistent.
      run {
        val reason = existingResult.internalInconsistencyReason
        if(reason != null) {
          val info = exec(app, reason)
          logger.requireEnd(app, info)
          return info
        }
      }

      // Check if flagged dirty.
      if(store.readTxn().use { it.isDirty(app) }) {
        val info = exec(app, DirtyFlaggedReason())
        logger.requireEnd(app, info)
        return info
      }

      // No inconsistencies found
      // Validate well-formedness of the dependency graph
      store.readTxn().use { layer.validate(app, existingResult, this, it) }
      // Mark not dirty.
      store.writeTxn().use { it.setIsDirty(app, false) }
      // Mark consistent this execution
      consistent[app] = existingResult
      // Cache result
      cache[app] = existingResult
      // Reuse existing result
      val info = ExecInfo(existingResult)
      logger.requireEnd(app, info)
      return info
    } finally {
      layer.requireEnd(app)
    }
  }

  // Method is open internal for testability
  open internal fun <I : In, O : Out> exec(app: FuncApp<I, O>, reason: ExecReason, useCache: Boolean = false): ExecInfo<I, O> {
    logger.rebuildStart(app, reason)
    val result = if(useCache) {
      share.reuseOrCreate(app, { store.readTxn().use { txn -> txn.resultsIn(it)?.cast<I, O>() } }) { this.execInternal(it) }
    } else {
      share.reuseOrCreate(app) { this.execInternal(it) }
    }
    logger.rebuildEnd(app, reason, result)
    return ExecInfo(result, reason)
  }

  // Method is open internal for testability
  open internal fun <I : In, O : Out> execInternal(app: FuncApp<I, O>): ExecRes<I, O> {
    val (builderId, input) = app
    val builder = getFunc<I, O>(builderId)
    val desc = builder.desc(input)
    val context = ExecContextImpl(this, store, app)
    val output = builder.exec(input, context)
    val (reqs, gens) = context.getReqsAndGens()
    val result = ExecRes(builderId, desc, input, output, reqs, gens)

    // Validate well-formedness of the dependency graph
    store.readTxn().use { layer.validate(app, result, this, it) }
    store.writeTxn().use {
      // Mark not dirty.
      it.setIsDirty(app, false)
      // Store result
      it.setResultsIn(app, result)
      // Store path dependencies
      context.writePathDepsToStore(it)
    }
    // Mark consistent this execution
    consistent[app] = result
    // Cache result
    cache[app] = result

    return result
  }
}

class DirtyFlaggedReason : ExecReason {
  override fun toString() = "flagged dirty"


  override fun equals(other: Any?): Boolean {
    if(this === other) return true
    if(other?.javaClass != javaClass) return false
    return true
  }

  override fun hashCode(): Int {
    return 0
  }
}