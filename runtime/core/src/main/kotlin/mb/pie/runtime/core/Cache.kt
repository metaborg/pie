package mb.pie.runtime.core


/**
 * Internal cache for task outputs and dependencies.
 */
interface Cache {
  operator fun set(task: UTask, data: UTaskData)
  operator fun get(task: UTask): UTaskData?
  fun drop()
}
