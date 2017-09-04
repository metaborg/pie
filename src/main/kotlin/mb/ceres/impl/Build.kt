package mb.ceres.impl

import com.google.inject.Injector
import mb.ceres.*
import mb.ceres.impl.store.BuildStore
import mb.ceres.impl.store.BuildStoreReadTxn
import mb.vfs.path.PPath
import java.util.*

interface Build {
  fun <I : In, O : Out> require(app: BuildApp<I, O>): BuildInfo<I, O>
}

open class BuildImpl(
  private val store: BuildStore,
  private val cache: BuildCache,
  private val share: BuildShare,
  private val reporter: BuildReporter,
  private val builders: Map<String, UBuilder>,
  private val injector: Injector)
  : Build {
  private val consistent = mutableMapOf<UBuildApp, UBuildRes>()
  private val stack = mutableSetOf<UBuildApp>()

  override fun <I : In, O : Out> require(app: BuildApp<I, O>): BuildInfo<I, O> {
    try {
      reporter.require(app)

      if (stack.contains(app)) {
        throw CyclicDependencyException(cycleError(app))
      }
      stack.add(app)

      val consistentResult = consistent[app]?.cast<I, O>()
      if (consistentResult != null) {
        // Existing result is known to be consistent this build: reuse
        reporter.consistent(app, consistentResult)
        return BuildInfo(consistentResult)
      }

      val existingResult = (cache[app] ?: store.readTxn().use { it.produces(app) })?.cast<I, O>()
      @Suppress("FoldInitializerAndIfToElvis")
      if (existingResult == null) {
        // No cached or stored result was found: rebuild
        val info = rebuild(app, NoResultReason(), true)
        reporter.consistent(app, info.result)
        return info
      }

      // Check for inconsistencies and rebuild when found
      // Internal consistency: output consistency
      run {
        val inconsistencyReason = existingResult.inconsistencyReason
        if (inconsistencyReason != null) {
          val info = rebuild(app, inconsistencyReason)
          reporter.consistent(app, info.result)
          return info
        }
      }

      // Internal consistency: generated files
      for (gen in existingResult.gens) {
        val (genPath, stamp) = gen
        val newStamp = stamp.stamper.stamp(genPath)
        reporter.checkGenPath(app, genPath, stamp, newStamp)
        if (stamp != newStamp) {
          // If a generated file is outdated (i.e., its stamp changed): rebuild
          val info = rebuild(app, InconsistentGenPath(existingResult, gen, newStamp))
          reporter.consistent(app, info.result)
          return info
        }
      }
      // Internal and total consistency: requirements
      for (req in existingResult.reqs) {
        val inconsistencyReason = req.makeConsistent(app, existingResult, this, reporter)
        if (inconsistencyReason != null) {
          val info = rebuild(app, inconsistencyReason)
          reporter.consistent(app, info.result)
          return info
        }
      }

      // No inconsistencies found
      // Validate well-formedness of the dependency graph
      validate(app, existingResult)
      reporter.consistent(app, existingResult)
      // Cache the result
      consistent[app] = existingResult
      cache[app] = existingResult
      // Reuse existing result
      return BuildInfo(existingResult)
    } finally {
      stack.remove(app)
    }
  }

  // Method is open internal for testability
  open internal fun <I : In, O : Out> rebuild(app: BuildApp<I, O>, reason: BuildReason, useCache: Boolean = false): BuildInfo<I, O> {
    reporter.build(app, reason)
    val result: BuildRes<I, O>
    try {
      result = if (useCache) {
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
    validate(app, result)
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

  private fun <I : In, O : Out> validate(app: BuildApp<I, O>, result: BuildRes<I, O>) {
    store.readTxn().use { txn ->
      for ((path, _) in result.gens) {
        val generatedBy = txn.generatedBy(path)
        if (generatedBy != null) {
          val builder = getBuilder<I, O>(result.builderId)
          // CHANGED: builders may describe if certain different build applications may generate the same file.
          @Suppress("UNCHECKED_CAST")
          if (!builder.mayOverlap(result.input, generatedBy.input as I)) {
            throw OverlappingGeneratedPathException(overlappingGenError(path, result, generatedBy))
          }
        }

        val requiredBy = txn.requiredBy(path)
        // CHANGED: it is allowed to generate something required by something on the stack
        if (requiredBy != null && !stack.contains(requiredBy)) {
          // CHANGED: 'path' is generated by 'result', and path is required by 'requiredBy', thus 'requiredBy' must (transitively) require 'app'.
          val requiredByResult = txn.produces(requiredBy)
          if (requiredByResult != null && !hasBuildReq(requiredByResult, app, txn)) {
            throw HiddenDependencyException(genAfterReqError(path, result, requiredBy))
          }
        }
      }

      for ((path, _) in result.reqs.filterIsInstance<PathReq>()) {
        val generator = txn.generatedBy(path)
        // 'path' is required by 'result', and path is generated by 'generator', thus 'result' must (transitively) require 'generator'.
        if (generator != null && !hasBuildReq(result, generator, txn)) {
          throw HiddenDependencyException(reqWithoutGenError(result, path, generator))
        }
      }
    }
  }


  private fun hasBuildReq(requiree: UBuildRes, generator: UBuildApp, txn: BuildStoreReadTxn): Boolean {
    // TODO: more efficient implementation for figuring out if a result depends on another result?
    val toCheckQueue: Queue<UBuildRes> = LinkedList()
    toCheckQueue.add(requiree)
    while (!toCheckQueue.isEmpty()) {
      val toCheck = toCheckQueue.poll()
      if (toCheck.requires(generator)) {
        return true
      }
      val reqRequests = toCheck.reqs.filterIsInstance<UBuildReq>().map { it.app }
      val reqResults = mutableListOf<UBuildRes>()
      for (reqRequest in reqRequests) {
        val reqResult = txn.produces(reqRequest) ?: error("Cannot get result for app $reqRequest")
        reqResults.add(reqResult)
      }
      toCheckQueue.addAll(reqResults)
    }
    return false
  }

  private fun <I : In, O : Out> getBuilder(id: String): Builder<I, O> {
    @Suppress("UNCHECKED_CAST")
    return (builders[id] ?: error("Builder with identifier '$id' does not exist")) as Builder<I, O>
  }


  private fun <I : In, O : Out> cycleError(app: BuildApp<I, O>): String {
    return """Cyclic dependency.
  Requirement of:

    $app

  from requirements:

    ${stack.joinToString(" -> ")}

  creates cycle
  """
  }

  private fun <I : In, O : Out> overlappingGenError(path: PPath, result: BuildRes<I, O>, generatedBy: UBuildApp?): String {
    return """Overlapping generated path.
  Path:

    $path

  was generated by:

    ${result.desc}

  and:

    $generatedBy
  """
  }

  private fun <I : In, O : Out> genAfterReqError(path: PPath, result: BuildRes<I, O>, requiredBy: UBuildApp?): String {
    return """Hidden dependency.
  Path:

    $path

  was generated by:

    ${result.desc}

  after being previously required by:

    $requiredBy
  """
  }

  private fun <I : In, O : Out> reqWithoutGenError(result: BuildRes<I, O>, path: PPath, generator: UBuildApp?): String {
    return """Hidden dependency.
  Build:

    ${result.desc}

  requires path:

    $path

  generated by:

    $generator

  without a (transitive) build requirement for it
  """
  }
}