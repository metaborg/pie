package mb.pie.runtime.core.impl

import com.google.inject.Injector
import mb.pie.runtime.core.*
import mb.pie.runtime.core.impl.store.BuildStore
import mb.pie.runtime.core.impl.store.BuildStoreReadTxn

interface Build {
  fun <I : In, O : Out> require(app: BuildApp<I, O>): BuildInfo<I, O>

  fun getUBuilder(id: String): UBuilder
  fun getAnyBuilder(id: String): AnyBuilder
  fun <I : In, O : Out> getBuilder(id: String): Builder<I, O>

  fun storeReadTxn(): BuildStoreReadTxn
}

open class BuildImpl(
  private val store: BuildStore,
  private val cache: BuildCache,
  private val share: BuildShare,
  private val validationLayer: ValidationLayer,
  private val reporter: BuildReporter,
  private val builders: Map<String, UBuilder>,
  private val injector: Injector)
  : Build {
  private val consistent = mutableMapOf<UBuildApp, UBuildRes>()


  override fun <I : In, O : Out> require(app: BuildApp<I, O>): BuildInfo<I, O> {
    try {
      validationLayer.requireStart(app)
      reporter.require(app)

      val consistentResult = consistent[app]?.cast<I, O>()
      if(consistentResult != null) {
        // Existing result is known to be consistent this build: reuse
        reporter.consistent(app, consistentResult)
        return BuildInfo(consistentResult)
      }

      val existingResult = (cache[app] ?: store.readTxn().use { it.produces(app) })?.cast<I, O>()
      @Suppress("FoldInitializerAndIfToElvis")
      if(existingResult == null) {
        // No cached or stored result was found: rebuild
        val info = rebuild(app, NoResultReason(), true)
        reporter.consistent(app, info.result)
        return info
      }

      // Check for inconsistencies and rebuild when found
      // Internal consistency: output consistency
      run {
        val inconsistencyReason = existingResult.inconsistencyReason
        if(inconsistencyReason != null) {
          val info = rebuild(app, inconsistencyReason)
          reporter.consistent(app, info.result)
          return info
        }
      }

      // Internal consistency: generated files
      for(gen in existingResult.gens) {
        val (genPath, stamp) = gen
        val newStamp = stamp.stamper.stamp(genPath)
        reporter.checkGenPath(app, genPath, stamp, newStamp)
        if(stamp != newStamp) {
          // If a generated file is outdated (i.e., its stamp changed): rebuild
          val info = rebuild(app, InconsistentGenPath(existingResult, gen, newStamp))
          reporter.consistent(app, info.result)
          return info
        }
      }
      // Internal and total consistency: requirements
      for(req in existingResult.reqs) {
        val inconsistencyReason = req.makeConsistent(app, existingResult, this, reporter)
        if(inconsistencyReason != null) {
          val info = rebuild(app, inconsistencyReason)
          reporter.consistent(app, info.result)
          return info
        }
      }

      // No inconsistencies found
      // Validate well-formedness of the dependency graph
      validationLayer.validate(app, existingResult, this)
      reporter.consistent(app, existingResult)
      // Cache the result
      consistent[app] = existingResult
      cache[app] = existingResult
      // Reuse existing result
      return BuildInfo(existingResult)
    } finally {
      validationLayer.requireEnd(app)
    }
  }

  // Method is open internal for testability
  open internal fun <I : In, O : Out> rebuild(app: BuildApp<I, O>, reason: BuildReason, useCache: Boolean = false): BuildInfo<I, O> {
    reporter.build(app, reason)
    val result: BuildRes<I, O>
    try {
      result = if(useCache) {
        share.reuseOrCreate(app, { store.readTxn().use { txn -> txn.produces(it)?.cast<I, O>() } }) { this.rebuildInternal(it) }
      } else {
        share.reuseOrCreate(app) { this.rebuildInternal(it) }
      }
    } catch(e: BuildException) {
      reporter.buildFailed(app, reason, e)
      throw e
    }
    reporter.buildSuccess(app, reason, result)
    return BuildInfo(result, reason)
  }

  // Method is open internal for testability
  open internal fun <I : In, O : Out> rebuildInternal(app: BuildApp<I, O>): BuildRes<I, O> {
    val (builderId, input) = app
    val builder = getBuilder<I, O>(builderId)
    val desc = builder.desc(input)
    val context = BuildContextImpl(this, store, injector, app)
    val output = context.use {
      builder.build(input, context)
    }
    val (reqs, gens) = context.getReqsAndGens()
    val result = BuildRes(builderId, desc, input, output, reqs, gens)

    // Validate well-formedness of the dependency graph
    validationLayer.validate(app, result, this)
    // Store result and path dependencies in build store
    store.writeTxn().use {
      it.setProduces(app, result)
      context.writePathDepsToStore(it)
    }
    // Cache result
    consistent[app] = result
    cache[app] = result

    return result
  }


  override fun getUBuilder(id: String): UBuilder {
    return (builders[id] ?: error("Builder with identifier '$id' does not exist"))
  }

  override fun getAnyBuilder(id: String): AnyBuilder {
    @Suppress("UNCHECKED_CAST")
    return getUBuilder(id) as AnyBuilder
  }

  override fun <I : In, O : Out> getBuilder(id: String): Builder<I, O> {
    @Suppress("UNCHECKED_CAST")
    return getUBuilder(id) as Builder<I, O>
  }


  override fun storeReadTxn(): BuildStoreReadTxn {
    return store.readTxn()
  }
}