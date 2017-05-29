package mb.ceres

import java.io.Serializable
import java.lang.IllegalStateException
import java.util.*


internal data class BuildRes<out I : In, out O : Out>(val builderId: String, val desc: String, val input: I, val output: O, val reqs: List<Req>, val gens: List<Gen>) : Serializable {
  val toApp get() = BuildApp<I, O>(builderId, input)
  fun requires(other: BuildApp<*, *>): Boolean {
    for ((req, _) in reqs.filterIsInstance<BuildReq<*, *>>()) {
      if (other == req) {
        return true
      }
    }
    return false
  }
}

data class BuildApp<out I : In, out O : Out>(val builderId: String, val input: I) : Serializable {
  constructor(builder: Builder<I, O>, input: I) : this(builder.id, input)
}


interface BuildManager {
  fun <I : In, O : Out> build(app: BuildApp<I, O>): O
  fun <I : In, O : Out> buildAll(vararg apps: BuildApp<I, O>): List<O>

  fun <I : In, O : Out> registerBuilder(builder: Builder<I, O>)
  fun <I : In, O : Out> unregisterBuilder(builder: Builder<I, O>)
}

internal interface BuildManagerInternal {
  fun <I : In, O : Out> require(app: BuildApp<I, O>): BuildRes<I, O>
}

internal typealias BuildMap = Map<BuildApp<*, *>, BuildRes<*, *>>
internal typealias PathMap = Map<CPath, BuildRes<*, *>>

internal open class BuildManagerImpl(resultCache: BuildMap = emptyMap(), generatedCache: PathMap = emptyMap()) : BuildManager, BuildManagerInternal {
  private val builders = mutableMapOf<String, Builder<*, *>>()

  private val resultCache = resultCache.toMutableMap()
  private val generatedByCache = generatedCache.toMutableMap()

  private val isConsistent = mutableSetOf<BuildApp<*, *>>()
  private val requiredBy = mutableMapOf<CPath, BuildRes<*, *>>()
  private val requireStack = mutableSetOf<BuildApp<*, *>>()


  private fun resetBeforeBuild() {
    isConsistent.clear()
    requiredBy.clear()
    requireStack.clear()
  }


  override fun <I : In, O : Out> build(app: BuildApp<I, O>): O {
    return buildInternal(app).output
  }

  open internal fun <I : In, O : Out> buildInternal(app: BuildApp<I, O>): BuildRes<I, O> {
    resetBeforeBuild()
    return require(app)
  }


  override fun <I : In, O : Out> buildAll(vararg apps: BuildApp<I, O>): List<O> {
    return buildAllInternal(*apps).map { it.output }
  }

  open internal fun <I : In, O : Out> buildAllInternal(vararg apps: BuildApp<I, O>): List<BuildRes<I, O>> {
    resetBeforeBuild()
    return apps.map { require(it) }
  }


  override fun <I : In, O : Out> require(app: BuildApp<I, O>): BuildRes<I, O> {
    try {
      if (requireStack.contains(app)) {
        throw CyclicDependencyException("Cyclic dependency: requirement of $app from requirements ${requireStack.joinToString(" -> ")} creates cycle")
      }
      requireStack.add(app)

      val cachedResult = getCachedResult(app)
      if (cachedResult == null) {
        // No existing result was found: rebuild
        return rebuild(app)
      }

      if (isConsistent.contains(app)) {
        // Existing result is known to be consistent this build: reuse
        return cachedResult
      }

      // Check for inconsistencies and rebuild when found
      // Internal consistency: generated files
      for ((genPath, stamp) in cachedResult.gens) {
        val newStamp = stamp.stamper.stamp(genPath)
        if (stamp != newStamp) {
          // If a generated file is outdated (i.e., its stamp changed): rebuild
          return rebuild(app)
        }
      }
      // Internal and total consistency: requirements
      for (req in cachedResult.reqs) {
        if (!req.makeConsistent(this)) {
          return rebuild(app)
        }
      }

      // No inconsistencies found
      // Validate well-formedness of the dependency graph
      validate(cachedResult)
      // Mark result consistent.
      isConsistent.add(app)
      // Reuse existing result
      return cachedResult
    } finally {
      requireStack.remove(app)
    }
  }

  open internal fun <I : In, O : Out> rebuild(app: BuildApp<I, O>): BuildRes<I, O> {
    val (builderId, input) = app
    val builder = getBuilder<I, O>(builderId)
    val desc = builder.desc(input)

    // Execute builder
    println("Executing builder $desc")
    val context = BuildContextImpl(this)
    val output = builder.build(input, context)
    val result = BuildRes(builderId, desc, input, output, context.reqs, context.gens)

    // Validate well-formedness of the dependency graph
    validate(result)
    // Cache result and mark it consistent
    resultCache.put(app, result)
    isConsistent.add(app)

    return result
  }

  open internal fun <I : In, O : Out> validate(result: BuildRes<I, O>) {
    // Clear own generated files from cache before validation, to prevent overlapping of own generated files
    for ((path, _) in result.gens) {
      generatedByCache.remove(path, result)
    }

    for ((path, _) in result.gens) {
      val generatedBy = generatedByCache[path]
      generatedByCache.put(path, result) // Add to cache before throwing exceptions
      if (generatedBy != null) {
        throw OverlappingGeneratedPathException("Overlapping generated path: $path was generated by '${result.desc}' and '${generatedBy.desc}'")
      }
      val requiredBy = requiredBy[path]
      if (requiredBy != null) {
        throw HiddenDependencyException("Hidden dependency: $path was generated by '${result.desc}' after being previously required by ${requiredBy.desc}")
      }
    }

    for ((path, _) in result.reqs.filterIsInstance<PathReq>()) {
      requiredBy.put(path, result)
      val generator = generatedByCache[path]
      // 'path' is required by 'result', and path is generated by 'generator', thus 'result' must (transitively) require 'generator'
      if (generator != null && !hasBuildReq(result, generator)) {
        throw HiddenDependencyException("Hidden dependency: '${result.desc}' requires path $path, generated by '${generator.desc}', without a (transitive) build requirement for it")
      }
    }
  }

  private fun hasBuildReq(requiree: BuildRes<*, *>, generator: BuildRes<*, *>): Boolean {
    // TODO: more efficient implementation for figuring out if a result depends on another result?
    val toCheckQueue: Queue<BuildRes<*, *>> = LinkedList()
    toCheckQueue.add(requiree)
    while (!toCheckQueue.isEmpty()) {
      val toCheck = toCheckQueue.poll()
      if (toCheck.requires(generator.toApp)) {
        return true
      }
      val reqRequests = toCheck.reqs.filterIsInstance<BuildReq<*, *>>().map { it.app }
      val reqResults = mutableListOf<BuildRes<*, *>>()
      for (reqRequest in reqRequests) {
        val reqResult = resultCache[reqRequest] ?: error("Cannot get result for app $reqRequest")
        reqResults.add(reqResult)
      }
      toCheckQueue.addAll(reqResults)
    }
    return false
  }


  private fun <I : In, O : Out> getBuilder(id: String): Builder<I, O> {
    if (!builders.containsKey(id)) {
      throw IllegalStateException("Builder with id $id does not exist")
    }
    @Suppress("UNCHECKED_CAST")
    return builders[id] as Builder<I, O>
  }

  private fun <I : In, O : Out> getCachedResult(app: BuildApp<I, O>): BuildRes<I, O>? {
    @Suppress("UNCHECKED_CAST")
    return resultCache[app] as BuildRes<I, O>?
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


open class BuildValidationException(message: String) : RuntimeException(message)
class OverlappingGeneratedPathException(message: String) : BuildValidationException(message)
class HiddenDependencyException(message: String) : BuildValidationException(message)
class CyclicDependencyException(message: String) : BuildValidationException(message)


internal class BuildContextImpl(val buildManager: BuildManagerInternal) : BuildContext {
  val reqs = mutableListOf<Req>()
  val gens = mutableListOf<Gen>()


  override fun <I : In, O : Out> require(app: BuildApp<I, O>, stamper: OutputStamper): O {
    val result = buildManager.require(app)
    val stamp = stamper.stamp(result.output)
    reqs.add(BuildReq(app, stamp))
    return result.output
  }

  override fun require(path: CPath, stamper: PathStamper) {
    val stamp = stamper.stamp(path)
    reqs.add(PathReq(path, stamp))
  }

  override fun generate(path: CPath, stamper: PathStamper) {
    val stamp = stamper.stamp(path)
    gens.add(Gen(path, stamp))
  }
}