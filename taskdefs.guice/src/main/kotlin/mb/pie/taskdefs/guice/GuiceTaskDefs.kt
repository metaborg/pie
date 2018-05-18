package mb.pie.taskdefs.guice

import com.google.inject.*
import com.google.inject.multibindings.MapBinder
import mb.pie.api.PieBuilder
import mb.pie.api.UTaskDef
import mb.pie.runtime.taskdefs.MapTaskDefs

fun PieBuilder.withGuiceTaskDefs(vararg modules: TaskDefsModule): PieBuilder {
  val injector = Guice.createInjector(modules.asList())
  withGuiceTaskDefs(injector)
  return this
}

fun PieBuilder.withGuiceTaskDefs(injector: Injector): PieBuilder {
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


fun Binder.taskDefsBinder(): MapBinder<String, UTaskDef> =
  MapBinder.newMapBinder(this, object : TypeLiteral<String>() {}, object : TypeLiteral<UTaskDef>() {})

inline fun <reified B : UTaskDef> Binder.bindTaskDef(builderBinder: MapBinder<String, UTaskDef>, id: String) {
  bind(B::class.java).`in`(Singleton::class.java)
  builderBinder.addBinding(id).to(B::class.java)
}

