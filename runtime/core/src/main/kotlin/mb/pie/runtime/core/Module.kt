package mb.pie.runtime.core

import com.google.inject.*
import com.google.inject.assistedinject.FactoryModuleBuilder
import com.google.inject.binder.LinkedBindingBuilder
import com.google.inject.binder.ScopedBindingBuilder
import com.google.inject.multibindings.MapBinder
import mb.pie.runtime.core.impl.BuildManagerImpl
import mb.pie.runtime.core.impl.cache.MapBuildCache
import mb.pie.runtime.core.impl.cache.NoopBuildCache
import mb.pie.runtime.core.impl.layer.ValidationBuildLayer
import mb.pie.runtime.core.impl.logger.StreamBuildLogger
import mb.pie.runtime.core.impl.share.BuildShare
import mb.pie.runtime.core.impl.share.CoroutineBuildShare
import mb.pie.runtime.core.impl.store.LMDBBuildStoreFactory

open class PieModule : Module {
  override fun configure(binder: Binder) {
    binder.bindBuildManager()
    binder.bindStore()
    binder.bindShare()
    binder.bindCache()
    binder.bindLayer()
    binder.bindLogger()

    val builders = binder.builderMapBinder()
    binder.bindBuilders(builders)
  }


  open protected fun Binder.bindBuildManager() {
    install(FactoryModuleBuilder()
      .implement(BuildManager::class.java, BuildManagerImpl::class.java)
      .build(BuildManagerFactory::class.java))
  }

  open protected fun Binder.bindStore() {
    bind<LMDBBuildStoreFactory>().asSingleton()
  }

  open protected fun Binder.bindShare() {
    bind<BuildShare>().toSingleton<CoroutineBuildShare>()
  }

  open protected fun Binder.bindCache() {
    bind<BuildCache>().to<NoopBuildCache>()
  }

  open protected fun Binder.bindLogger() {
    bind<BuildLogger>().to<StreamBuildLogger>()
  }

  open protected fun Binder.bindLayer() {
    bind<BuildLayer>().to<ValidationBuildLayer>()
  }


  open protected fun Binder.bindBuilders(builders: MapBinder<String, UBuilder>) {

  }


  inline protected fun <I : In, O : Out, reified B : Builder<I, O>> Binder.bindBuilder(builders: MapBinder<String, UBuilder>, builder: B) {
    bind<B>().toObject(builder)
    builders.addBinding(builder.id).toInstance(builder)
  }

  protected fun bindClasslessBuilder(builders: MapBinder<String, UBuilder>, builder: UBuilder) {
    builders.addBinding(builder.id).toInstance(builder)
  }
}

inline fun <reified T> Binder.bind() = bind(T::class.java)!!

inline fun <reified T> LinkedBindingBuilder<in T>.to() = to(T::class.java)!!

inline fun <reified T> LinkedBindingBuilder<in T>.toSingleton() = to(T::class.java)!!.asSingleton()

inline fun <reified T> LinkedBindingBuilder<in T>.toObject(instance: T) = toInstance(instance)

inline fun ScopedBindingBuilder.asSingleton() = `in`(Singleton::class.java)


inline fun Binder.builderMapBinder(): MapBinder<String, UBuilder> {
  return MapBinder.newMapBinder(this, object : TypeLiteral<String>() {}, object : TypeLiteral<UBuilder>() {})
}

inline fun <reified B : UBuilder> Binder.bindBuilder(builderBinder: MapBinder<String, UBuilder>, id: String) {
  bind<B>().asSingleton()
  builderBinder.addBinding(id).to<B>()
}

fun Binder.bindBuilder(builderBinder: MapBinder<String, UBuilder>, builder: UBuilder) {
  builderBinder.addBinding(builder.id).toInstance(builder)
}
