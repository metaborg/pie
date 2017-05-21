package mb.ceres

import java.io.Serializable
import java.lang.IllegalStateException


interface BuildContext {
  fun <I : In, O : Out> require(request: BuildRequest<I, O>): O?
  fun require(path: CPath, stamper: Stamper)
  fun generate(path: CPath, stamper: Stamper)
}

typealias In = Serializable
typealias Out = Serializable

interface Builder<in I : In, out O : Out> {
  val id: String

  fun desc(input: I): String
  fun build(input: I, context: BuildContext): O?
}


interface Stamper {
  fun stamp(path: CPath): Stamp
}

interface Stamp : Serializable {
  val stamper: Stamper
}

data class ValueStamp<out V>(val value: V?, override val stamper: Stamper) : Stamp


sealed class Req : Serializable
data class FileReq(val path: CPath, val stamp: Stamp) : Req()
data class BuildReq<out I : In, out O : Out>(val request: BuildRequest<I, O>) : Req()


data class Gen(val path: CPath, val stamp: Stamp) : Serializable


data class BuildResult<out I : In, out O : Out>(val builderId: String, val input: I, val output: O?, val reqs: List<Req>, val gens: List<Gen>) : Serializable
data class BuildRequest<out I : In, out O : Out>(val builderId: String, val input: I) : Serializable


interface BuildManager {
  fun <I : In, O : Out> registerBuilder(builder: Builder<I, O>)
  fun <I : In, O : Out> unregisterBuilder(builder: Builder<I, O>)

  fun <I : In, O : Out> build(vararg requests: BuildRequest<I, O>): Collection<BuildResult<I, O>>
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


  override fun <I : In, O : Out> build(vararg requests: BuildRequest<I, O>): List<BuildResult<I, O>> {
    consistent.clear()
    required.clear()
    generated.clear()
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
        is FileReq -> {
          val (reqPath, stamp) = req
          val newStamp = stamp.stamper.stamp(reqPath)
          if (stamp != newStamp) {
            // If a required file is outdated (i.e., its stamp changed): rebuild
            return rebuild(request)
          }
        }
        is BuildReq<*, *> -> {
          // Make required build cache
          require(req.request)
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


  override fun <I : In, O : Out> require(request: BuildRequest<I, O>): O? {
    val result = buildManager.require(request)
    reqs.add(BuildReq(request))
    return result.output
  }

  override fun require(path: CPath, stamper: Stamper) {
    val stamp = stamper.stamp(path)
    reqs.add(FileReq(path, stamp))
  }

  override fun generate(path: CPath, stamper: Stamper) {
    val stamp = stamper.stamp(path)
    gens.add(Gen(path, stamp))
  }
}