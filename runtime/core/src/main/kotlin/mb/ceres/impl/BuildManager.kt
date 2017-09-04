package mb.ceres.impl

import com.google.inject.Inject
import com.google.inject.Injector
import com.google.inject.assistedinject.Assisted
import mb.ceres.*
import mb.ceres.impl.store.BuildStore

class BuildSessionImpl(private val build: BuildImpl, private val injector: Injector)
  : BuildSession {
  override fun <I : In, O : Out> buildToInfo(app: BuildApp<I, O>): BuildInfo<I, O> {
    return build.require(app)
  }

  override fun <I : In, O : Out> buildAllToInfo(vararg apps: BuildApp<I, O>): List<BuildInfo<I, O>> {
    return apps.map {
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
    return apps.map {
      build.require(it)
    }
  }


  override fun <I : In, O : Out> build(app: BuildApp<I, O>): O = buildToInfo(app).result.output

  override fun <I : In, O : Out> buildAll(vararg apps: BuildApp<I, O>): List<O> {
    return apps.map {
      build.require(it).result.output
    }
  }

  override fun <I : In, O : Out, B : Builder<I, O>> build(clazz: Class<B>, input: I): O = buildToInfo(clazz, input).result.output

  override fun <I : In, O : Out, B : Builder<I, O>> buildAll(clazz: Class<B>, vararg inputs: I): List<O> {
    val builder = injector.getInstance(clazz)
    val apps = inputs.map { BuildApp(builder, it) }
    return apps.map {
      build.require(it).result.output
    }
  }
}

class BuildManagerImpl @Inject constructor(
  private @Assisted val store: BuildStore,
  private @Assisted val cache: BuildCache,
  private val share: BuildShare,
  private val builders: MutableMap<String, UBuilder>,
  private val injector: Injector)
  : BuildManager {
  override fun newSession(): BuildSession {
    val reporter = injector.getInstance(BuildReporter::class.java)
    return BuildSessionImpl(BuildImpl(store, cache, share, reporter, builders, injector), injector)
  }

  override fun dropStore() {
    store.writeTxn().use { it.drop() }
  }

  override fun dropCache() {
    cache.drop()
  }


  override fun <I : In, O : Out> build(app: BuildApp<I, O>): O {
    return newSession().build(app)
  }

  override fun <I : In, O : Out> buildAll(vararg apps: BuildApp<I, O>): List<O> {
    return newSession().buildAll(*apps)
  }

  override fun <I : In, O : Out, B : Builder<I, O>> build(clazz: Class<B>, input: I): O {
    return newSession().build(clazz, input)
  }

  override fun <I : In, O : Out, B : Builder<I, O>> buildAll(clazz: Class<B>, vararg inputs: I): List<O> {
    return newSession().buildAll(clazz, *inputs)
  }

  override fun <I : In, O : Out> buildToInfo(app: BuildApp<I, O>): BuildInfo<I, O> {
    return newSession().buildToInfo(app)
  }

  override fun <I : In, O : Out> buildAllToInfo(vararg apps: BuildApp<I, O>): List<BuildInfo<I, O>> {
    return newSession().buildAllToInfo(*apps)
  }

  override fun <I : In, O : Out, B : Builder<I, O>> buildToInfo(clazz: Class<B>, input: I): BuildInfo<I, O> {
    return newSession().buildToInfo(clazz, input)
  }

  override fun <I : In, O : Out, B : Builder<I, O>> buildAllToInfo(clazz: Class<B>, vararg inputs: I): List<BuildInfo<I, O>> {
    return newSession().buildAllToInfo(clazz, *inputs)
  }
}

