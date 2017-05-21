package mb.ceres

import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.lang.IllegalStateException
import java.nio.file.Files
import java.nio.file.StandardOpenOption


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
  fun path(input: I): CPath
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

class BuildManagerImpl(prevGenerated: Map<CPath, CPath> = emptyMap()) : BuildManager, BuildManagerInternal {
  private val builders = mutableMapOf<String, Builder<*, *>>()

  private val consistent = mutableSetOf<CPath>()
  private val required = mutableSetOf<CPath>()
  private val generated = prevGenerated.toMutableMap()


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

  internal fun <I : In, O : Out> getBuilder(id: String): Builder<I, O> {
    if (!builders.containsKey(id)) {
      throw IllegalStateException("Builder with id $id does not exist")
    }
    @Suppress("UNCHECKED_CAST")
    return builders[id] as Builder<I, O>
  }


  override fun <I : In, O : Out> build(vararg requests: BuildRequest<I, O>): List<BuildResult<I, O>> {
    return requests.map { require(it) }
  }

  override fun <I : In, O : Out> require(request: BuildRequest<I, O>): BuildResult<I, O> {
    val (builderId, input) = request
    val builder = getBuilder<I, O>(builderId)
    val path = builder.path(input)

    val existingResult = readResult<I, O>(path)
    if (existingResult == null) {
      // No existing result was found: rebuild
      return rebuild(request)
    }

    if (path in consistent) {
      // If javaPath is consistent (i.e., builder was executed with input already): reuse
      return existingResult
    }

    // Check for inconsistencies and rebuild when found
    // Internal consistency: builder or input change
    if (existingResult.builderId != builderId || existingResult.input != input) {
      // If result was built with a different builder, or built with a different input: rebuild
      return rebuild(request)
    }
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
          // Make required build consistent
          require(req.request)
        }
      }
    }

    // No inconsistencies found
    // Validate well-formedness of the dependency graph
    validate(existingResult, path)
    // Mark result at javaPath as consistent. Important: must be done after validation
    consistent.add(path)
    // Reuse existing result
    return existingResult
  }

  internal fun <I : In, O : Out> rebuild(request: BuildRequest<I, O>): BuildResult<I, O> {
    val (builderId, input) = request
    val builder = getBuilder<I, O>(builderId)
    val path = builder.path(input)
    val desc = builder.desc(input)

    // Execute builder
    println("Executing builder $desc")
    val requirer = BuildContextImpl(this)
    val output = builder.build(input, requirer)
    val result = BuildResult(builderId, input, output, requirer.reqs, requirer.gens)

    // Store build result
    writeResult(result, path)
    // Validate well-formedness of the dependency graph
    validate(result, path)
    // Mark result at javaPath as consistent. Important: must be done after validation
    consistent.add(path)

    return result
  }

  internal fun <I : In, O : Out> validate(result: BuildResult<I, O>, path: CPath) {
    if (path in consistent) {
      error("Overlapping build results for javaPath $path")
    }
    for ((genPath, _) in result.gens) {
      if (generated[genPath] != null) {
        error("Overlapping generated javaPath $genPath")
      } else if (genPath in required) {
        error("Hidden dependency on javaPath $genPath")
      } else {
        generated.put(genPath, path)
      }
    }
    // TODO: more complex hidden dependency check
  }

  internal fun <I : In, O : Out> readResult(path: CPath): BuildResult<I, O>? {
    if (Files.notExists(path.javaPath)) {
      return null
    }

    Files.newInputStream(path.javaPath).use {
      ObjectInputStream(it).use {
        @Suppress("UNCHECKED_CAST")
        return it.readObject() as BuildResult<I, O>
      }
    }
  }

  internal fun <I : In, O : Out> writeResult(result: BuildResult<I, O>, path: CPath) {
    Files.createDirectories(path.javaPath.parent)
    Files.newOutputStream(path.javaPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING).use {
      ObjectOutputStream(it).use {
        it.writeObject(result)
      }
    }
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