package mb.pie.runtime.core

import mb.pie.runtime.core.impl.BuildCache
import mb.pie.runtime.core.impl.store.BuildStore

interface BuildSession {
  @Throws(BuildException::class)
  fun <I : In, O : Out> build(app: BuildApp<I, O>): O

  @Throws(BuildException::class)
  fun <I : In, O : Out> buildAll(vararg apps: BuildApp<I, O>): List<O>

  @Throws(BuildException::class)
  fun <I : In, O : Out, B : Builder<I, O>> build(clazz: Class<B>, input: I): O

  @Throws(BuildException::class)
  fun <I : In, O : Out, B : Builder<I, O>> buildAll(clazz: Class<B>, vararg inputs: I): List<O>


  @Throws(BuildException::class)
  fun <I : In, O : Out> buildToInfo(app: BuildApp<I, O>): BuildInfo<I, O>

  @Throws(BuildException::class)
  fun <I : In, O : Out> buildAllToInfo(vararg apps: BuildApp<I, O>): List<BuildInfo<I, O>>

  @Throws(BuildException::class)
  fun <I : In, O : Out, B : Builder<I, O>> buildToInfo(clazz: Class<B>, input: I): BuildInfo<I, O>

  @Throws(BuildException::class)
  fun <I : In, O : Out, B : Builder<I, O>> buildAllToInfo(clazz: Class<B>, vararg inputs: I): List<BuildInfo<I, O>>
}


@Throws(BuildException::class)
inline fun <I : In, O : Out, reified B : Builder<I, O>> BuildSession.build(input: I): O {
  return this.build(B::class.java, input)
}

@Throws(BuildException::class)
inline fun <I : In, O : Out, reified B : Builder<I, O>> BuildSession.buildAll(vararg inputs: I): List<O> {
  return this.buildAll(B::class.java, *inputs)
}

@Throws(BuildException::class)
inline fun <I : In, O : Out, reified B : Builder<I, O>> BuildSession.buildToInfo(input: I): BuildInfo<I, O> {
  return this.buildToInfo(B::class.java, input)
}

@Throws(BuildException::class)
inline fun <I : In, O : Out, reified B : Builder<I, O>> BuildSession.buildAllToInfo(vararg inputs: I): List<BuildInfo<I, O>> {
  return this.buildAllToInfo(B::class.java, *inputs)
}


interface BuildManager : BuildSession {
  fun newSession(): BuildSession

  fun dropStore()
  fun dropCache()
}


interface BuildManagerFactory {
  fun create(store: BuildStore, cache: BuildCache): BuildManager
}

open class BuildValidationException(message: String) : RuntimeException(message)
class OverlappingGeneratedPathException(message: String) : BuildValidationException(message)
class HiddenDependencyException(message: String) : BuildValidationException(message)
class CyclicDependencyException(message: String) : BuildValidationException(message)
