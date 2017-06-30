package mb.ceres.impl

import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.assistedinject.Assisted
import mb.ceres.*

class BuildManagerImpl @Inject constructor(
  private @Assisted val store: BuildStore,
  private @Assisted val cache: BuildCache,
  private val share: BuildShare,
  private val builders: MutableMap<String, UBuilder>,
  private val injector: Injector)
  : BuildManager {
  override fun <I : In, O : Out> buildToInfo(app: BuildApp<I, O>): BuildInfo<I, O> {
    val reporter = injector.getInstance(BuildReporter::class.java)
    val build = BuildImpl(store, cache, share, reporter, builders, injector)
    return build.require(app)
  }

  override fun <I : In, O : Out> buildAllToInfo(vararg apps: BuildApp<I, O>): List<BuildInfo<I, O>> {
    val reporter = injector.getInstance(BuildReporter::class.java)
    return apps.map {
      val build = BuildImpl(store, cache, share, reporter, builders, injector)
      build.require(it)
    }
  }

  override fun <I : In, O : Out, B : Builder<I, O>> buildToInfo(clazz: Class<B>, input: I): BuildInfo<I, O> {
    val builder = injector.getInstance(clazz)
    val app = BuildApp(builder, input)
    return buildToInfo(app)
  }

  override fun <I : In, O : Out, B : Builder<I, O>> buildAllToInfo(clazz: Class<B>, vararg inputs: I): List<BuildInfo<I, O>> {
    val builder = injector.getInstance(clazz)
    val apps = inputs.map { BuildApp(builder, it) }
    val reporter = injector.getInstance(BuildReporter::class.java)
    return apps.map {
      val build = BuildImpl(store, cache, share, reporter, builders, injector)
      build.require(it)
    }
  }


  override fun <I : In, O : Out> build(app: BuildApp<I, O>): O = buildToInfo(app).result.output

  override fun <I : In, O : Out> buildAll(vararg apps: BuildApp<I, O>): List<O> {
    val reporter = injector.getInstance(BuildReporter::class.java)
    return apps.map {
      val build = BuildImpl(store, cache, share, reporter, builders, injector)
      build.require(it).result.output
    }
  }

  override fun <I : In, O : Out, B : Builder<I, O>> build(clazz: Class<B>, input: I): O = buildToInfo(clazz, input).result.output

  override fun <I : In, O : Out, B : Builder<I, O>> buildAll(clazz: Class<B>, vararg inputs: I): List<O> {
    val builder = injector.getInstance(clazz)
    val apps = inputs.map { BuildApp(builder, it) }
    val reporter = injector.getInstance(BuildReporter::class.java)
    return apps.map {
      val build = BuildImpl(store, cache, share, reporter, builders, injector)
      build.require(it).result.output
    }
  }


  override fun dropStore() {
    store.drop()
  }

  override fun dropCache() {
    cache.drop()
  }
}

