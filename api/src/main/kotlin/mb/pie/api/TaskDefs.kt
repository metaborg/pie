package mb.pie.api

/**
 * Collection of [task definitions][TaskDef].
 */
interface TaskDefs {
  fun <I : In, O : Out> getTaskDef(id: String): TaskDef<I, O>?
}
