package mb.pie.runtime.taskdefs

import mb.pie.api.*

/**
 * Task definitions from an immutable map.
 */
open class MapTaskDefs(
  private val taskDefs: Map<String, UTaskDef>
) : TaskDefs {
  override fun getUTaskDef(id: String): UTaskDef {
    return (taskDefs[id] ?: throw RuntimeException("Task definition with identifier '$id' does not exist"))
  }

  override fun getGTaskDef(id: String): GTaskDef {
    @Suppress("UNCHECKED_CAST")
    return getUTaskDef(id) as GTaskDef
  }

  override fun <I : In, O : Out> getTaskDef(id: String): TaskDef<I, O> {
    @Suppress("UNCHECKED_CAST")
    return getUTaskDef(id) as TaskDef<I, O>
  }
}
