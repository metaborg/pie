package mb.pie.runtime.core

import com.google.inject.*
import com.google.inject.assistedinject.FactoryModuleBuilder
import com.google.inject.binder.LinkedBindingBuilder
import com.google.inject.binder.ScopedBindingBuilder
import com.google.inject.multibindings.MapBinder
import mb.pie.runtime.core.exec.*
import mb.pie.runtime.core.impl.cache.NoopCache
import mb.pie.runtime.core.impl.exec.BottomUpExecutorImpl
import mb.pie.runtime.core.impl.exec.TopDownExecutorImpl
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

    val taskDefsBinder = binder.taskDefsBinder()
    binder.bindTaskDefs(taskDefsBinder)
  }


  protected open fun Binder.bindExecutors() {
    install(FactoryModuleBuilder()
      .implement(TopDownExecutor::class.java, TopDownExecutorImpl::class.java)
      .build(TopDownExecutorFactory::class.java))
    install(FactoryModuleBuilder()
      .implement(BottomUpExecutor::class.java, BottomUpExecutorImpl::class.java)
      .build(BottomUpExecutorFactory::class.java))
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


  protected open fun Binder.bindTaskDefs(builders: MapBinder<String, UTaskDef>) {

  }
}

inline fun <reified T> Binder.bind() = bind(T::class.java)!!

inline fun <reified T> LinkedBindingBuilder<in T>.to() = to(T::class.java)!!

inline fun <reified T> LinkedBindingBuilder<in T>.toSingleton() = to(T::class.java)!!.asSingleton()

@Suppress("NOTHING_TO_INLINE")
inline fun ScopedBindingBuilder.asSingleton() = `in`(Singleton::class.java)


@Suppress("NOTHING_TO_INLINE")
inline fun Binder.taskDefsBinder(): MapBinder<String, UTaskDef> {
  return MapBinder.newMapBinder(this, object : TypeLiteral<String>() {}, object : TypeLiteral<UTaskDef>() {})
}

inline fun <reified B : UTaskDef> Binder.bindTaskDef(builderBinder: MapBinder<String, UTaskDef>, id: String) {
  bind<B>().asSingleton()
  builderBinder.addBinding(id).to<B>()
}
