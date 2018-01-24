package mb.pie.runtime.core

import com.google.inject.*
import com.google.inject.assistedinject.FactoryModuleBuilder
import com.google.inject.binder.LinkedBindingBuilder
import com.google.inject.binder.ScopedBindingBuilder
import com.google.inject.multibindings.MapBinder
import mb.pie.runtime.core.exec.*
import mb.pie.runtime.core.impl.cache.NoopCache
import mb.pie.runtime.core.impl.exec.*
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


  protected open fun Binder.bindExecutors() {
    install(FactoryModuleBuilder()
      .implement(PullingExecutor::class.java, PullingExecutorImpl::class.java)
      .build(PullingExecutorFactory::class.java))
    install(FactoryModuleBuilder()
      .implement(DirtyFlaggingExecutor::class.java, DirtyFlaggingExecutorImpl::class.java)
      .build(DirtyFlaggingExecutorFactory::class.java))
    install(FactoryModuleBuilder()
      .implement(ObservingExecutor::class.java, ObservingExecutorImpl::class.java)
      .build(ObservingExecutorFactory::class.java))
  }

  protected open fun Binder.bindStore() {
    bind<LMDBBuildStoreFactory>().asSingleton()
  }

  protected open fun Binder.bindShare() {
    bind<Share>().toSingleton<CoroutineShare>()
  }

  protected open fun Binder.bindCache() {
    bind<Cache>().to<NoopCache>()
  }

  protected open fun Binder.bindLogger() {
    bind<Logger>().to<NoopLogger>()
  }

  protected open fun Binder.bindLayer() {
    bind<Layer>().to<ValidationLayer>()
  }


  protected open fun Binder.bindFuncs(builders: MapBinder<String, UFunc>) {

  }


  protected inline fun <I : In, O : Out, reified F : Func<I, O>> Binder.bindBuilder(funcs: MapBinder<String, UFunc>, func: F) {
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

inline fun <reified B : UFunc> Binder.bindFunc(builderBinder: MapBinder<String, UFunc>) {
  bind<B>().asSingleton()
  builderBinder.addBinding(B::class.java.canonicalName!!).to<B>()
}

fun Binder.bindFunc(builderBinder: MapBinder<String, UFunc>, builder: UFunc) {
  builderBinder.addBinding(builder.id).toInstance(builder)
}
