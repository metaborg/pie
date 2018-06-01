package mb.pie.share.coroutine

import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.sync.Mutex
import mb.pie.api.*

/**
 * Sets the share of this builder to the [CoroutineShare].
 */
fun PieBuilder.withCoroutineShare(): PieBuilder {
  this.withShare { CoroutineShare() }
  return this
}

/**
 * [Share] implementation that shares concurrently executing tasks using Kotlin coroutines.
 */
class CoroutineShare : Share {
  private val deferredTasks = mutableMapOf<TaskKey, Deferred<UTaskData>>()
  private val mutex = Mutex()


  override fun share(key: TaskKey, execFunc: () -> TaskData<*, *>, visitedFunc: () -> TaskData<*, *>?): TaskData<*, *> {
    return runBlocking { getResult(key, execFunc, visitedFunc) }
  }

  private suspend fun CoroutineScope.getResult(key: TaskKey, execFunc: () -> TaskData<*, *>, visitedFunc: () -> TaskData<*, *>?): TaskData<*, *> {
    mutex.lock()

    val existingDeferredExec = deferredTasks[key]
    if(existingDeferredExec != null) {
      // There is already a deferred execution, wait for its result.
      mutex.unlock()
      @Suppress("UNCHECKED_CAST")
      return existingDeferredExec.await()
    }

    /*
    First check if task was already visited. This handles the case where a deferred task was removed from deferredTasks before another
    coroutine could acquire the first lock, causing a recomputation.
    */
    val visited = visitedFunc()
    if(visited != null) {
      mutex.unlock()
      return visited
    }

    // Task has not been visited yet, execute the task asynchronously.
    val deferredExec: Deferred<UTaskData>
    try {
      deferredExec = async(coroutineContext) { execFunc() }
      deferredTasks[key] = deferredExec
    } finally {
      mutex.unlock()
    }

    try {
      // Wait for the deferred execution to end, to return its data.
      return deferredExec.await()
    } finally {
      // Remove deferred task when done.
      mutex.lock()
      deferredTasks.remove(key)
      mutex.unlock()
    }
  }


  override fun toString(): String {
    return "CoroutineShare"
  }
}
