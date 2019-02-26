package mb.pie.runtime.taskdefs

import mb.pie.api.*

/**
 * Task definitions from an immutable map.
 */
public open class MapTaskDefs : TaskDefs {
  private val taskDefs: Map<String, TaskDef<*, *>>;

  constructor(taskDefs: Map<String, TaskDef<*, *>>) {
    this.taskDefs = taskDefs;
  }

  override fun <I : In, O : Out> getTaskDef(id: String): TaskDef<I, O>? {
    @Suppress("UNCHECKED_CAST")
    return taskDefs[id] as TaskDef<I, O>?;
  }
}
