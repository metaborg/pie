package mb.pie.runtime.core.impl

import com.google.inject.Inject
import com.google.inject.Provider
import com.google.inject.assistedinject.Assisted
import mb.pie.runtime.core.*


class PullingExecutorImpl @Inject constructor(
  private @Assisted val store: Store,
  private @Assisted val cache: Cache,
  private val share: Share,
  private val layer: Provider<Layer>,
  private val funcs: MutableMap<String, UFunc>,
  private val logger: Logger
) : PullingExecutor {
  override fun newExec() = PullingExecImpl(store, cache, share, layer.get(), logger, funcs)
  override fun dropStore() {
    store.writeTxn().use { it.drop() }
    store.sync()
  }

  override fun dropCache() = cache.drop()
}

open class PullingExecImpl(
  private val store: Store,
  private val cache: Cache,
  private val share: Share,
  private val layer: Layer,
  private val logger: Logger,
  private val funcs: Map<String, UFunc>
) : PullingExec, Exec, Funcs by FuncsImpl(funcs) {
  private val consistent = mutableMapOf<UFuncApp, UExecRes>()


  fun <I : In, O : Out> requireInitial(app: FuncApp<I, O>): ExecInfo<I, O> {
    try {
      logger.requireInitialStart(app)
      val info = require(app)
      logger.requireInitialEnd(app, info)
      return info
    } finally {
      store.sync()
    }
  }

  override fun <I : In, O : Out> require(app: FuncApp<I, O>): ExecInfo<I, O> {
    try {
      layer.requireStart(app)
      logger.requireStart(app)

      // Check if builder application is already consistent this build.
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

      // Check for inconsistencies and rebuild when found.
      // Internal consistency: output consistency
      run {
        val inconsistencyReason = existingResult.internalInconsistencyReason
        if(inconsistencyReason != null) {
          val info = exec(app, inconsistencyReason)
          logger.requireEnd(app, info)
          return info
        }
      }

      // Internal consistency: generated files
      for(gen in existingResult.gens) {
        val (genPath, stamp) = gen
        logger.checkGenStart(app, gen)
        val newStamp = stamp.stamper.stamp(genPath)
        if(stamp != newStamp) {
          // If a generated file is outdated (i.e., its stamp changed): rebuild
          val reason = InconsistentGenPath(existingResult, gen, newStamp)
          logger.checkGenEnd(app, gen, reason)
          val info = exec(app, reason)
          logger.requireEnd(app, info)
          return info
        } else {
          logger.checkGenEnd(app, gen, null)
        }
      }

      // Internal and total consistency: requirements
      for(req in existingResult.reqs) {
        val inconsistencyReason = req.makeConsistent(app, existingResult, this, logger)
        if(inconsistencyReason != null) {
          val info = exec(app, inconsistencyReason)
          logger.requireEnd(app, info)
          return info
        }
      }

      // No inconsistencies found
      // Validate well-formedness of the dependency graph
      store.readTxn().use { layer.validate(app, existingResult, this, it) }
      // Cache the result
      consistent[app] = existingResult
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
    // Store result and path dependencies in build store
    store.writeTxn().use {
      it.setResultsIn(app, result)
      context.writePathDepsToStore(it)
    }
    // Cache result
    consistent[app] = result
    cache[app] = result

    return result
  }


  override fun <I : In, O : Out> requireOutput(app: FuncApp<I, O>) = requireInitial(app).result.output
  override fun <I : In, O : Out> requireInfo(app: FuncApp<I, O>) = requireInitial(app)
}
