package mb.pie.runtime.core.impl.exec

import com.google.inject.Inject
import com.google.inject.Provider
import com.google.inject.assistedinject.Assisted
import mb.pie.runtime.core.*
import mb.pie.runtime.core.exec.PullingExec
import mb.pie.runtime.core.exec.PullingExecutor
import mb.pie.runtime.core.impl.*
import mb.util.async.Cancelled
import mb.util.async.NullCancelled


class PullingExecutorImpl @Inject constructor(
  @Assisted private val store: Store,
  @Assisted private val cache: Cache,
  private val share: Share,
  private val layer: Provider<Layer>,
  private val logger: Provider<Logger>,
  private val funcs: MutableMap<String, UFunc>
) : PullingExecutor {
  override fun exec() = PullingExecImpl(store, cache, share, layer.get(), logger.get(), funcs)


  override fun dropStore() {
    store.writeTxn().use { it.drop() }
    store.sync()
  }

  override fun dropCache() {
    cache.drop()
  }
}

open class PullingExecImpl(
  private val store: Store,
  private val cache: Cache,
  private val share: Share,
  private val layer: Layer,
  private val logger: Logger,
  private val funcs: Map<String, UFunc>
) : PullingExec, Exec, Funcs by FuncsImpl(funcs) {
  private val visited = mutableMapOf<UFuncApp, UExecRes>()


  fun <I : In, O : Out> requireInitial(app: FuncApp<I, O>, cancel: Cancelled = NullCancelled()): ExecInfo<I, O> {
    try {
      logger.requireTopDownInitialStart(app)
      val info = require(app, cancel)
      logger.requireTopDownInitialEnd(app, info)
      return info
    } finally {
      store.sync()
    }
  }

  override fun <I : In, O : Out> require(app: FuncApp<I, O>, cancel: Cancelled): ExecInfo<I, O> {
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
          val info = exec(app, inconsistencyReason, cancel)
          logger.requireTopDownEnd(app, info)
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
          val info = exec(app, reason, cancel)
          logger.requireTopDownEnd(app, info)
          return info
        } else {
          logger.checkGenEnd(app, gen, null)
        }
      }

      // Internal and total consistency: requirements
      for(req in existingResult.reqs) {
        val inconsistencyReason = req.makeConsistent(app, existingResult, this, cancel, logger)
        if(inconsistencyReason != null) {
          val info = exec(app, inconsistencyReason, cancel)
          logger.requireTopDownEnd(app, info)
          return info
        }
      }

      // No inconsistencies found
      // Validate well-formedness of the dependency graph
      store.readTxn().use { layer.validate(app, existingResult, this, it) }
      // Cache the result
      visited[app] = existingResult
      cache[app] = existingResult
      // Reuse existing result
      val info = ExecInfo(existingResult)
      logger.requireTopDownEnd(app, info)
      return info
    } finally {
      layer.requireTopDownEnd(app)
    }
  }

  // Method is open internal for testability
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

  // Method is open internal for testability
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
      store.readTxn().use { layer.validate(app, result, this, it) }
      // Store result and path dependencies in build store
      store.writeTxn().use {
        it.setResultOf(app, result)
        context.writePathDepsToStore(it)
      }
      // Cache result
      visited[app] = result
      cache[app] = result

      return result
    } catch(e: InterruptedException) {
      store.writeTxn().use {
        context.writePathDepsToStore(it)
      }
      throw e
    }
  }


  override fun <I : In, O : Out> requireOutput(app: FuncApp<I, O>, cancel: Cancelled) = requireInitial(app, cancel).result.output
  override fun <I : In, O : Out> requireInfo(app: FuncApp<I, O>, cancel: Cancelled) = requireInitial(app, cancel)
}
