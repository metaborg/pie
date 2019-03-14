package mb.pie.share.coroutine

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import mb.pie.api.PieBuilder
import mb.pie.api.Share
import mb.pie.api.TaskData
import mb.pie.api.TaskKey
import java.util.function.Supplier

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
  private val deferredTasks = mutableMapOf<TaskKey, Deferred<TaskData<*, *>>>()
  private val mutex = Mutex()


  override fun share(key: TaskKey, execFunc: Supplier<TaskData<*, *>>, visitedFunc: Supplier<TaskData<*, *>>?): TaskData<*, *>? {
    return runBlocking { getResult(key, execFunc, visitedFunc) }
  }

  private suspend fun CoroutineScope.getResult(key: TaskKey, execFunc: Supplier<TaskData<*, *>>, visitedFunc: Supplier<TaskData<*, *>>?): TaskData<*, *> {
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
    val visited = visitedFunc?.get()
    if(visited != null) {
      mutex.unlock()
      return visited
    }

    // Task has not been visited yet, execute the task asynchronously.
    val deferredExec: Deferred<TaskData<*, *>>
    try {
      deferredExec = coroutineScope { async(coroutineContext) { execFunc.get() } }
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
