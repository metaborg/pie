package mb.ceres

import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.nio.file.Files


interface BuildContext {
  fun <I, O> require(request: BuildRequest<I, O>): O?
  fun require(path: CPath, stamper: Stamper)
  fun generate(path: CPath, stamper: Stamper)
}

interface Builder<in I, out O> : Serializable {
  fun name(input: I): String
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
data class BuildReq<I, out O>(val request: BuildRequest<I, O>) : Req()


data class Gen(val path: CPath, val stamp: Stamp) : Serializable


data class BuildResult<I, out O>(val builder: Builder<I, O>, val input: I, val output: O?, val reqs: List<Req>, val gens: List<Gen>) : Serializable {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other?.javaClass != javaClass) return false

    other as BuildResult<*, *>

    if (builder::class != other.builder::class) return false
    if (input != other.input) return false
    if (output != other.output) return false
    if (reqs != other.reqs) return false
    if (gens != other.gens) return false

    return true
  }

  override fun hashCode(): Int {
    var result = 0
    result = 31 * result + builder::class.hashCode()
    result = 31 * result + (input?.hashCode() ?: 0)
    result = 31 * result + (output?.hashCode() ?: 0)
    result = 31 * result + reqs.hashCode()
    result = 31 * result + gens.hashCode()
    return result
  }
}

data class BuildRequest<I, out O>(val builder: Builder<I, O>, val input: I) : Serializable {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other?.javaClass != javaClass) return false

    other as BuildResult<*, *>

    if (builder::class != other.builder::class) return false
    if (input != other.input) return false

    return true
  }

  override fun hashCode(): Int {
    var result = 0
    result = 31 * result + builder::class.hashCode()
    result = 31 * result + (input?.hashCode() ?: 0)
    return result
  }
}


interface BuildManager {
  fun <I, O> build(vararg requests: BuildRequest<I, O>): Collection<BuildResult<I, O>>
}

internal interface BuildManagerInternal {
  fun <I, O> require(request: BuildRequest<I, O>): BuildResult<I, O>
}

class BuildManagerImpl(prevGenerated: Map<CPath, CPath> = emptyMap()) : BuildManager, BuildManagerInternal {
  private val consistent = mutableSetOf<CPath>()
  private val required = mutableSetOf<CPath>()
  private val generated = prevGenerated.toMutableMap()


  override fun <I, O> build(vararg requests: BuildRequest<I, O>): List<BuildResult<I, O>> {
    return requests.map { require(it) }
  }

  override fun <I, O> require(request: BuildRequest<I, O>): BuildResult<I, O> {
    val (builder, input) = request
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
    if (existingResult.builder != builder || existingResult.input != input) {
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

  fun <I, O> rebuild(request: BuildRequest<I, O>): BuildResult<I, O> {
    val (builder, input) = request
    val path = builder.path(input)
    val name = builder.name(input)

    // Execute builder
    println("Executing builder $name")
    val requirer = BuildContextImpl(this)
    val output = builder.build(input, requirer)
    val result = BuildResult(builder, input, output, requirer.reqs, requirer.gens)

    // Store build result
    writeResult(result, path)
    // Validate well-formedness of the dependency graph
    validate(result, path)
    // Mark result at javaPath as consistent. Important: must be done after validation
    consistent.add(path)

    return result
  }

  fun <I, O> validate(result: BuildResult<I, O>, path: CPath) {
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

  fun <I, O> readResult(path: CPath): BuildResult<I, O>? {
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

  fun <I, O> writeResult(result: BuildResult<I, O>, path: CPath) {
    Files.newOutputStream(path.javaPath).use {
      ObjectOutputStream(it).use {
        it.writeObject(result)
      }
    }
  }
}


internal class BuildContextImpl(val buildManager: BuildManagerInternal) : BuildContext {
  val reqs = mutableListOf<Req>()
  val gens = mutableListOf<Gen>()


  override fun <I, O> require(request: BuildRequest<I, O>): O? {
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