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
  private val sharedTasks = mutableMapOf<UTask, Deferred<UTaskData>>()
  private val mutex = Mutex()


  override fun reuseOrCreate(key: TaskKey, cacheFunc: (TaskKey) -> UTaskData?, execFunc: (UTask) -> UTaskData): UTaskData {
    return runBlocking { getResult(key, cacheFunc, execFunc) }
  }

  override fun reuseOrCreate(key: TaskKey, execFunc: (UTask) -> UTaskData): UTaskData {
    return runBlocking { getResult(key, null, execFunc) }
  }


  private suspend fun CoroutineScope.getResult(app: UTask, cacheFunc: ((UTask) -> UTaskData?)?, execFunc: (UTask) -> UTaskData): UTaskData {
    mutex.lock()

    val existingBuild = sharedTasks[app]
    if(existingBuild != null) {
      // There is already a build for given app, wait for its result
      mutex.unlock()
      @Suppress("UNCHECKED_CAST")
      return existingBuild.await()
    }

    if(cacheFunc != null) {
      /* First check if there is already a cached value. This handles the case where a build was removed from
      sharedTasks before another coroutine could acquire the first lock, causing a recomputation. */
      val cached = cacheFunc(app)
      if(cached != null) {
        mutex.unlock()
        return cached
      }
    }

    // There is no build for given app yet, execute and share it
    val exec: Deferred<UTaskData>
    try {
      exec = async(coroutineContext) { execFunc(app) }
      sharedTasks[app] = exec
    } finally {
      mutex.unlock()
    }

    try {
      return exec.await()
    } finally {
      // Remove shared build after it is finished
      mutex.lock()
      sharedTasks.remove(app)
      mutex.unlock()
    }
  }


  override fun toString(): String {
    return "CoroutineShare"
  }
}
