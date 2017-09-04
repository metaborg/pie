package mb.ceres

import com.google.inject.Binder
import com.google.inject.Module
import com.google.inject.Singleton
import com.google.inject.TypeLiteral
import com.google.inject.assistedinject.FactoryModuleBuilder
import com.google.inject.binder.LinkedBindingBuilder
import com.google.inject.binder.ScopedBindingBuilder
import com.google.inject.multibindings.MapBinder
import mb.ceres.impl.*
import mb.ceres.impl.store.LMDBBuildStoreFactory

open class CeresModule : Module {
  override fun configure(binder: Binder) {
    binder.bindBuildManager()
    binder.bindStore()
    binder.bindShare()
    binder.bindReporter()

    val builders = binder.builderMapBinder()
    binder.bindBuilders(builders)
  }


  open protected fun Binder.bindBuildManager() {
    install(FactoryModuleBuilder()
      .implement(BuildManager::class.java, BuildManagerImpl::class.java)
      .build(BuildManagerFactory::class.java))
  }

  open protected fun Binder.bindStore() {
    bind<LMDBBuildStoreFactory>().asSingleton();
  }

  open protected fun Binder.bindShare() {
    bind<BuildShare>().toSingleton<BuildShareImpl>()
  }

  open protected fun Binder.bindReporter() {
    bind<BuildReporter>().to<StreamBuildReporter>()
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
