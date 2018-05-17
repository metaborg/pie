package mb.pie.runtime.taskdefs

import mb.pie.api.*

/**
 * Task definitions from a mutable map.
 */
open class MutableMapTaskDefs : TaskDefs {
  private val taskDefs = mutableMapOf<String, UTaskDef>()

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

  fun addTaskDef(id: String, taskDef: UTaskDef) {
    taskDefs[id] = taskDef
  }

  fun removeTaskDef(id: String) {
    taskDefs.remove(id)
  }
}
