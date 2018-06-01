package mb.pie.runtime.taskdefs

import mb.pie.api.*

/**
 * Task definitions from an immutable map.
 */
open class MapTaskDefs(private val taskDefs: Map<String, UTaskDef>) : TaskDefs {
  override fun <I : In, O : Out> getTaskDef(id: String): TaskDef<I, O>? {
    @Suppress("UNCHECKED_CAST")
    return taskDefs[id] as TaskDef<I, O>?
  }
}
