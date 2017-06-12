package mb.ceres

import mb.ceres.impl.BuildCache
import mb.ceres.internal.BuildStore

interface BuildManager {
  @Throws(BuildException::class)
  fun <I : In, O : Out> build(app: BuildApp<I, O>): O
  @Throws(BuildException::class)
  fun <I : In, O : Out> buildAll(vararg apps: BuildApp<I, O>): List<O>

  @Throws(BuildException::class)
  fun <I : In, O : Out, B : Builder<I, O>> build(clazz: Class<B>, input: I): O
  @Throws(BuildException::class)
  fun <I : In, O : Out, B : Builder<I, O>> buildAll(clazz: Class<B>, vararg inputs: I): List<O>
}

@Throws(BuildException::class)
inline fun <I : In, O : Out, reified B : Builder<I, O>> BuildManager.build(input: I): O {
  return this.build(B::class.java, input)
}

@Throws(BuildException::class)
inline fun <I : In, O : Out, reified B : Builder<I, O>> BuildManager.buildAll(vararg inputs: I): List<O> {
  return this.buildAll(B::class.java, *inputs)
}


interface BuildManagerFactory {
  fun create(store: BuildStore, cache: BuildCache): BuildManager
}

open class BuildValidationException(message: String) : RuntimeException(message)
class OverlappingGeneratedPathException(message: String) : BuildValidationException(message)
class HiddenDependencyException(message: String) : BuildValidationException(message)
class CyclicDependencyException(message: String) : BuildValidationException(message)
