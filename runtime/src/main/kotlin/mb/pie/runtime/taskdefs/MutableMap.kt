package mb.pie.runtime.taskdefs

import mb.pie.api.*

/**
 * Task definitions from a mutable map.
 */
public open class MutableMapTaskDefs : TaskDefs {
  private val taskDefs: MutableMap<String, TaskDef<*, *>> = mutableMapOf<String, TaskDef<*, *>>()

  override fun <I : In, O : Out> getTaskDef(id: String): TaskDef<I, O>? {
    @Suppress("UNCHECKED_CAST")
    return taskDefs.get(id) as TaskDef<I, O>?;
  }

  fun add(id: String, taskDef: TaskDef<*, *>) {
    taskDefs[id] = taskDef;
  }

  fun remove(id: String) {
    taskDefs.remove(id);
  }
}
