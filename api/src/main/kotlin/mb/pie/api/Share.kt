package mb.pie.api

/**
 * Share for concurrently executing tasks.
 */
interface Share {
  fun reuseOrCreate(key: TaskKey, cacheFunc: (TaskKey) -> UTaskData?, execFunc: (UTask) -> UTaskData): UTaskData
  fun reuseOrCreate(key: TaskKey, execFunc: (UTask) -> UTaskData): UTaskData
}
