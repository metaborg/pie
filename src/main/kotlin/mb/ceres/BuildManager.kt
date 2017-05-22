package mb.ceres

import java.io.Serializable
import java.lang.IllegalStateException


data class BuildResult<out I : In, out O : Out>(val builderId: String, val input: I, val output: O, val reqs: List<Req>, val gens: List<Gen>) : Serializable
data class BuildRequest<out I : In, out O : Out>(val builderId: String, val input: I) : Serializable {
  constructor(builder: Builder<I, O>, input: I) : this(builder.id, input)
}


interface BuildManager {
  fun <I : In, O : Out> build(request: BuildRequest<I, O>): O
  fun <I : In, O : Out> buildAll(vararg requests: BuildRequest<I, O>): List<O>

  fun <I : In, O : Out> registerBuilder(builder: Builder<I, O>)
  fun <I : In, O : Out> unregisterBuilder(builder: Builder<I, O>)
}

internal interface BuildManagerInternal {
  fun <I : In, O : Out> require(request: BuildRequest<I, O>): BuildResult<I, O>
}

class BuildManagerImpl(prevConsistent: Map<BuildRequest<*, *>, BuildResult<*, *>> = emptyMap(), prevGenerated: Map<CPath, CPath> = emptyMap()) : BuildManager, BuildManagerInternal {
  private val builders = mutableMapOf<String, Builder<*, *>>()
  private val cache = prevConsistent.toMutableMap()

  private val consistent = mutableSetOf<BuildRequest<*, *>>()
  private val required = mutableSetOf<CPath>()
  private val generated = prevGenerated.toMutableMap()


  private fun resetBeforeBuild() {
    consistent.clear()
    required.clear()
    generated.clear()
  }


  override fun <I : In, O : Out> build(request: BuildRequest<I, O>): O {
    return buildInternal(request).output
  }

  internal fun <I : In, O : Out> buildInternal(request: BuildRequest<I, O>): BuildResult<I, O> {
    resetBeforeBuild()
    return require(request)
  }


  override fun <I : In, O : Out> buildAll(vararg requests: BuildRequest<I, O>): List<O> {
    return buildAllInternal(*requests).map { it.output }
  }

  internal fun <I : In, O : Out> buildAllInternal(vararg requests: BuildRequest<I, O>): List<BuildResult<I, O>> {
    resetBeforeBuild()
    return requests.map { require(it) }
  }


  override fun <I : In, O : Out> require(request: BuildRequest<I, O>): BuildResult<I, O> {
    val existingResult = getCachedResult(request)
    if (existingResult == null) {
      // No existing result was found: rebuild
      return rebuild(request)
    }

    if (consistent.contains(request)) {
      // Existing result is known to be consistent this build: reuse
      return existingResult
    }

    // Check for inconsistencies and rebuild when found
    // Internal consistency: generated files
    for ((genPath, stamp) in existingResult.gens) {
      val newStamp = stamp.stamper.stamp(genPath)
      if (stamp != newStamp) {
        // If a generated file is outdated (i.e., its stamp changed): rebuild
        return rebuild(request)
      }
    }
    // Internal consistency: required files
    // Total consistency: required builds
    for (req in existingResult.reqs) {
      when (req) {
      // TODO: move consistency code into requirement, so we don't have to pattern match here
        is FileReq -> {
          val (reqPath, stamp) = req
          val newStamp = stamp.stamper.stamp(reqPath)
          if (stamp != newStamp) {
            // If a required file is outdated (i.e., its stamp changed): rebuild
            return rebuild(request)
          }
        }
        is BuildReq<*, *> -> {
          val (reqRequest, stamp) = req
          // Make required build consistent
          val result = require(reqRequest)
          // CHANGED: paper algorithm did not check if the result changed, which would cause inconsistencies
          val newStamp = stamp.stamper.stamp(result.output)
          if (stamp != newStamp) {
            // If output of a required builder has changed: rebuild
            return rebuild(request)
          }
        }
      }
    }

    // No inconsistencies found
    // Validate well-formedness of the dependency graph
    validate(existingResult)
    // Mark result consistent. Important: must be done after validation
    consistent.add(request)
    // Reuse existing result
    return existingResult
  }

  internal fun <I : In, O : Out> rebuild(request: BuildRequest<I, O>): BuildResult<I, O> {
    val (builderId, input) = request
    val builder = getBuilder<I, O>(builderId)
    val desc = builder.desc(input)

    // Execute builder
    println("Executing builder $desc")
    val requirer = BuildContextImpl(this)
    val output = builder.build(input, requirer)
    val result = BuildResult(builderId, input, output, requirer.reqs, requirer.gens)

    // Validate well-formedness of the dependency graph
    validate(result)
    // Cache result and mark it consistent. Important: must be done after validation
    cache.put(request, result)
    consistent.add(request)

    return result
  }

  internal fun <I : In, O : Out> validate(result: BuildResult<I, O>) {
    // TODO: overlapping file check
    // TODO: hidden dependency check
  }


  internal fun <I : In, O : Out> getBuilder(id: String): Builder<I, O> {
    if (!builders.containsKey(id)) {
      throw IllegalStateException("Builder with id $id does not exist")
    }
    @Suppress("UNCHECKED_CAST")
    return builders[id] as Builder<I, O>
  }

  internal fun <I : In, O : Out> getCachedResult(request: BuildRequest<I, O>): BuildResult<I, O>? {
    @Suppress("UNCHECKED_CAST")
    return cache[request] as BuildResult<I, O>?
  }


  override fun <I : In, O : Out> registerBuilder(builder: Builder<I, O>) {
    val id = builder.id
    if (builders.containsKey(id)) {
      throw IllegalStateException("Builder with id $id already exists")
    }
    builders.put(id, builder)
  }

  override fun <I : In, O : Out> unregisterBuilder(builder: Builder<I, O>) {
    val id = builder.id
    if (!builders.containsKey(id)) {
      throw IllegalStateException("Builder with id $id does not exist")
    }
    builders.remove(id)
  }
}


internal class BuildContextImpl(val buildManager: BuildManagerInternal) : BuildContext {
  val reqs = mutableListOf<Req>()
  val gens = mutableListOf<Gen>()


  override fun <I : In, O : Out> require(request: BuildRequest<I, O>, stamper: OutputStamper): O {
    val result = buildManager.require(request)
    val stamp = stamper.stamp(result.output)
    reqs.add(BuildReq(request, stamp))
    return result.output
  }

  override fun require(path: CPath, stamper: PathStamper) {
    val stamp = stamper.stamp(path)
    reqs.add(FileReq(path, stamp))
  }

  override fun generate(path: CPath, stamper: PathStamper) {
    val stamp = stamper.stamp(path)
    gens.add(Gen(path, stamp))
  }
}