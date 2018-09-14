package mb.pie.runtime.taskdefs

import mb.pie.api.*

/**
 * Task definitions from a mutable map.
 */
open class MutableMapTaskDefs : TaskDefs {
  private val taskDefs = mutableMapOf<String, TaskDef<*, *>>()

  override fun <I : In, O : Out> getTaskDef(id: String): TaskDef<I, O>? {
    @Suppress("UNCHECKED_CAST")
    return taskDefs[id] as TaskDef<I, O>?
  }

  fun add(id: String, taskDef: TaskDef<*, *>) {
    taskDefs[id] = taskDef
  }

  fun remove(id: String) {
    taskDefs.remove(id)
  }
}
