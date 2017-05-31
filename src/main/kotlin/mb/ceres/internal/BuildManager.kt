package mb.ceres.internal

import com.google.inject.Inject
import com.google.inject.assistedinject.Assisted
import mb.ceres.*

class BuildManagerImpl @Inject constructor(private @Assisted val store: Store, private val builderStore: BuilderStore) : BuildManager, BuilderStore by builderStore {
  override fun <I : In, O : Out> build(app: BuildApp<I, O>): O {
    val build = BuildImpl(store, builderStore)
    return build.require(app).output
  }

  override fun <I : In, O : Out> buildAll(vararg apps: BuildApp<I, O>): List<O> {
    val build = BuildImpl(store, builderStore)
    return apps.map { build.require(it).output }
  }
}

