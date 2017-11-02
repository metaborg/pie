package mb.pie.runtime.core

import com.google.inject.*
import com.google.inject.assistedinject.FactoryModuleBuilder
import com.google.inject.binder.LinkedBindingBuilder
import com.google.inject.binder.ScopedBindingBuilder
import com.google.inject.multibindings.MapBinder
import mb.pie.runtime.core.impl.PullingExecutorImpl
import mb.pie.runtime.core.impl.PushingExecutorImpl
import mb.pie.runtime.core.impl.cache.NoopCache
import mb.pie.runtime.core.impl.layer.ValidationLayer
import mb.pie.runtime.core.impl.logger.NoopLogger
import mb.pie.runtime.core.impl.share.CoroutineShare
import mb.pie.runtime.core.impl.store.LMDBBuildStoreFactory


open class PieModule : Module {
  override fun configure(binder: Binder) {
    binder.bindExecutors()
    binder.bindStore()
    binder.bindShare()
    binder.bindCache()
    binder.bindLayer()
    binder.bindLogger()

    val builders = binder.funcsMapBinder()
    binder.bindFuncs(builders)
  }


  open protected fun Binder.bindExecutors() {
    install(FactoryModuleBuilder()
      .implement(PullingExecutor::class.java, PullingExecutorImpl::class.java)
      .build(PullingExecutorFactory::class.java))
    install(FactoryModuleBuilder()
      .implement(PushingExecutor::class.java, PushingExecutorImpl::class.java)
      .build(PushingExecutorFactory::class.java))
  }

  open protected fun Binder.bindStore() {
    bind<LMDBBuildStoreFactory>().asSingleton()
  }

  open protected fun Binder.bindShare() {
    bind<Share>().toSingleton<CoroutineShare>()
  }

  open protected fun Binder.bindCache() {
    bind<Cache>().to<NoopCache>()
  }

  open protected fun Binder.bindLogger() {
    bind<Logger>().to<NoopLogger>()
  }

  open protected fun Binder.bindLayer() {
    bind<Layer>().to<ValidationLayer>()
  }


  open protected fun Binder.bindFuncs(builders: MapBinder<String, UFunc>) {

  }


  inline protected fun <I : In, O : Out, reified F : Func<I, O>> Binder.bindBuilder(funcs: MapBinder<String, UFunc>, func: F) {
    bind<F>().toObject(func)
    funcs.addBinding(func.id).toInstance(func)
  }

  protected fun bindClasslessBuilder(builders: MapBinder<String, UFunc>, builder: UFunc) {
    builders.addBinding(builder.id).toInstance(builder)
  }
}

inline fun <reified T> Binder.bind() = bind(T::class.java)!!

inline fun <reified T> LinkedBindingBuilder<in T>.to() = to(T::class.java)!!

inline fun <reified T> LinkedBindingBuilder<in T>.toSingleton() = to(T::class.java)!!.asSingleton()

inline fun <reified T> LinkedBindingBuilder<in T>.toObject(instance: T) = toInstance(instance)

@Suppress("NOTHING_TO_INLINE")
inline fun ScopedBindingBuilder.asSingleton() = `in`(Singleton::class.java)


@Suppress("NOTHING_TO_INLINE")
inline fun Binder.funcsMapBinder(): MapBinder<String, UFunc> {
  return MapBinder.newMapBinder(this, object : TypeLiteral<String>() {}, object : TypeLiteral<UFunc>() {})
}

inline fun <reified B : UFunc> Binder.bindFunc(builderBinder: MapBinder<String, UFunc>, id: String) {
  bind<B>().asSingleton()
  builderBinder.addBinding(id).to<B>()
}

fun Binder.bindFunc(builderBinder: MapBinder<String, UFunc>, builder: UFunc) {
  builderBinder.addBinding(builder.id).toInstance(builder)
}
