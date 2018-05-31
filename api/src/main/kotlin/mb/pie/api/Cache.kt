package mb.pie.api

/**
 * Cache for task outputs and dependencies.
 */
interface Cache {
  operator fun set(key: TaskKey, data: UTaskData)
  operator fun get(key: TaskKey): UTaskData?
  fun drop()
}
