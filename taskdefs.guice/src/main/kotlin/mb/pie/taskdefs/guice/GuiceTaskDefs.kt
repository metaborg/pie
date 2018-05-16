package mb.pie.taskdefs.guice

import com.google.inject.*
import com.google.inject.binder.LinkedBindingBuilder
import com.google.inject.binder.ScopedBindingBuilder
import com.google.inject.multibindings.MapBinder
import mb.pie.api.*

fun PieBuilder.withGuiceTaskDefs(vararg modules: TaskDefsModule): PieBuilder {
  val injector = Guice.createInjector(modules.asList())
  val taskDefsMap = injector.getInstance(Key.get(object : TypeLiteral<MutableMap<String, UTaskDef>>() {}))
  val taskDefs = MapTaskDefs(taskDefsMap)
  this.withTaskDefs(taskDefs)
  return this
}


abstract class TaskDefsModule : Module {
  override fun configure(binder: Binder) {
    val taskDefsBinder = binder.taskDefsBinder()
    binder.bindTaskDefs(taskDefsBinder)
  }

  abstract fun Binder.bindTaskDefs(builders: MapBinder<String, UTaskDef>)
}


fun Binder.taskDefsBinder(): MapBinder<String, UTaskDef> {
  return MapBinder.newMapBinder(this, object : TypeLiteral<String>() {}, object : TypeLiteral<UTaskDef>() {})
}

inline fun <reified B : UTaskDef> Binder.bindTaskDef(builderBinder: MapBinder<String, UTaskDef>, id: String) {
  bind<B>().asSingleton()
  builderBinder.addBinding(id).to<B>()
}

inline fun <reified T> Binder.bind() = bind(T::class.java)!!
inline fun <reified T> LinkedBindingBuilder<in T>.to() = to(T::class.java)!!
inline fun <reified T> LinkedBindingBuilder<in T>.toSingleton() = to(T::class.java)!!.asSingleton()
fun ScopedBindingBuilder.asSingleton() = `in`(Singleton::class.java)
