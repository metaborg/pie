package mb.ceres.internal

import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.assistedinject.Assisted
import mb.ceres.*
import mb.ceres.impl.BuildCache

class BuildManagerImpl @Inject constructor(
        private @Assisted val store: BuildStore,
        private @Assisted val cache: BuildCache,
        private val share: BuildShare,
        private val builders: MutableMap<String, UBuilder>,
        private val injector: Injector)
  : BuildManager {
  override fun <I : In, O : Out> build(app: BuildApp<I, O>): O {
    val build = BuildImpl(store, cache, share, builders, injector)
    return build.require(app).output
  }

  override fun <I : In, O : Out> buildAll(vararg apps: BuildApp<I, O>): List<O> {
    return apps.map {
      val build = BuildImpl(store, cache, share, builders, injector)
      build.require(it).output
    }
  }

  override fun <I : In, O : Out, B : Builder<I, O>> build(clazz: Class<B>, input: I): O {
    val builder = injector.getInstance(clazz)
    val app = BuildApp(builder, input)
    return build(app)
  }

  override fun <I : In, O : Out, B : Builder<I, O>> buildAll(clazz: Class<B>, vararg inputs: I): List<O> {
    val builder = injector.getInstance(clazz)
    val apps = inputs.map { BuildApp(builder, it) }
    return apps.map {
      val build = BuildImpl(store, cache, share, builders, injector)
      build.require(it).output
    }
  }

  override fun dropStore() {
    store.drop();
  }

  override fun dropCache() {
    cache.drop();
  }
}

