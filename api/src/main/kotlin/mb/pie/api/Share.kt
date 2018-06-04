package mb.pie.api

/**
 * Share for concurrently executing tasks.
 */
interface Share {
  fun share(key: TaskKey, execFunc: () -> TaskData<*, *>, visitedFunc: () -> TaskData<*, *>?): TaskData<*, *>
}
