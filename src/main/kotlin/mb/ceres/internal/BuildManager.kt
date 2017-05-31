package mb.ceres.internal

import com.google.inject.Inject
import com.google.inject.assistedinject.Assisted
import mb.ceres.*

class BuildManagerImpl @Inject constructor(private @Assisted val store: Store, private val builderStore: BuilderStore, private val share: BuildShare) : BuildManager, BuilderStore by builderStore {
  override fun <I : In, O : Out> build(app: BuildApp<I, O>): O {
    val build = BuildImpl(store, builderStore, share)
    return build.require(app).output
  }

  override fun <I : In, O : Out> buildAll(vararg apps: BuildApp<I, O>): List<O> {
    return apps.map {
      val build = BuildImpl(store, builderStore, share)
      build.require(it).output
    }
  }
}

