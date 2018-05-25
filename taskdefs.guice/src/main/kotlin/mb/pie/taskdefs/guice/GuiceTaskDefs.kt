package mb.pie.taskdefs.guice

import com.google.inject.*
import com.google.inject.multibindings.MapBinder
import mb.pie.api.*
import mb.pie.runtime.taskdefs.MapTaskDefs

/**
 * Sets the task definitions of this builder to the [GuiceTaskDefs] retrieved from given [injector].
 */
fun PieBuilder.withGuiceTaskDefs(injector: Injector): PieBuilder {
  val taskDefs = injector.getInstance(GuiceTaskDefs::class.java)
  this.withTaskDefs(taskDefs)
  return this
}

/**
 * Sets the task definitions of this builder to the given [taskDefs].
 */
fun PieBuilder.withGuiceTaskDefs(taskDefs: GuiceTaskDefs): PieBuilder {
  this.withTaskDefs(taskDefs)
  return this
}


/**
 * [TaskDefs] implementation that injects the map binding created from [TaskDefsModule].
 */
class GuiceTaskDefs @Inject constructor(map: MutableMap<String, UTaskDef>) : MapTaskDefs(map)

/**
 * A module that binds [GuiceTaskDefs] as a singleton, which can be passed to a [PieBuilder] with [withGuiceTaskDefs].
 */
class GuiceTaskDefsModule : Module {
  override fun configure(binder: Binder) {
    binder.bind(GuiceTaskDefs::class.java).`in`(Singleton::class.java)
  }
}


/**
 * A module for binding task definitions. Extend this module, and implement the [bindTaskDefs] function by making calls to [bindTaskDef] to
 * bind task definitions.
 */
abstract class TaskDefsModule : Module {
  override fun configure(binder: Binder) {
    val taskDefsBinder = MapBinder.newMapBinder<String, UTaskDef>(binder, object : TypeLiteral<String>() {}, object : TypeLiteral<UTaskDef>() {})
    binder.bindTaskDefs(taskDefsBinder)
  }

  abstract fun Binder.bindTaskDefs(builders: MapBinder<String, UTaskDef>)

  protected inline fun <reified B : UTaskDef> Binder.bindTaskDef(builderBinder: MapBinder<String, UTaskDef>, id: String) {
    bind(B::class.java).`in`(Singleton::class.java)
    builderBinder.addBinding(id).to(B::class.java)
  }
}
