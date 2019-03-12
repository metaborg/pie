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
class GuiceTaskDefs @Inject constructor(map: HashMap<String, TaskDef<*, *>>) : MapTaskDefs(map)

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
    val taskDefsBinder = MapBinder.newMapBinder<String, TaskDef<*, *>>(binder, object : TypeLiteral<String>() {}, object : TypeLiteral<TaskDef<*, *>>() {})
    binder.bindTaskDefs(taskDefsBinder)
  }

  abstract fun Binder.bindTaskDefs(taskDefsBinder: MapBinder<String, TaskDef<*, *>>)

  protected inline fun <reified B : TaskDef<*, *>> Binder.bindTaskDef(builderBinder: MapBinder<String, TaskDef<*, *>>, id: String) {
    bind(B::class.java).`in`(Singleton::class.java)
    builderBinder.addBinding(id).to(B::class.java)
  }
}
