package mb.ceres

import mb.ceres.internal.Store

interface BuildManager : BuilderStore {
  fun <I : In, O : Out> build(app: BuildApp<I, O>): O
  fun <I : In, O : Out> buildAll(vararg apps: BuildApp<I, O>): List<O>
}

interface BuilderStore {
  fun <I : In, O : Out> registerBuilder(builder: Builder<I, O>)
  fun <I : In, O : Out> unregisterBuilder(builder: Builder<I, O>)
  fun <I : In, O : Out> getBuilder(id: String): Builder<I, O>
}

interface BuildManagerFactory {
  fun create(store: Store): BuildManager
}

open class BuildValidationException(message: String) : RuntimeException(message)
class OverlappingGeneratedPathException(message: String) : BuildValidationException(message)
class HiddenDependencyException(message: String) : BuildValidationException(message)
class CyclicDependencyException(message: String) : BuildValidationException(message)
