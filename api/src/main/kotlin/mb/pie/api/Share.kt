package mb.pie.api

/**
 * Share for concurrently executing tasks.
 */
interface Share {
  fun reuseOrCreate(task: UTask, cacheFunc: (UTask) -> UTaskData?, execFunc: (UTask) -> UTaskData): UTaskData
  fun reuseOrCreate(task: UTask, execFunc: (UTask) -> UTaskData): UTaskData
}
