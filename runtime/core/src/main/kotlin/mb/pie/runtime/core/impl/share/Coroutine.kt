package mb.pie.runtime.core.impl.share

import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.sync.Mutex
import mb.pie.runtime.core.*


class CoroutineShare : Share {
  private val sharedBuilds = mutableMapOf<UFuncApp, Deferred<UFuncAppData>>()
  private val mutex = Mutex()


  override fun reuseOrCreate(app: UFuncApp, cacheFunc: (UFuncApp) -> UFuncAppData?, execFunc: (UFuncApp) -> UFuncAppData): UFuncAppData {
    return runBlocking { getResult(app, cacheFunc, execFunc) }
  }

  override fun reuseOrCreate(app: UFuncApp, execFunc: (UFuncApp) -> UFuncAppData): UFuncAppData {
    return runBlocking { getResult(app, null, execFunc) }
  }


  private suspend fun CoroutineScope.getResult(app: UFuncApp, cacheFunc: ((UFuncApp) -> UFuncAppData?)?, execFunc: (UFuncApp) -> UFuncAppData): UFuncAppData {
    mutex.lock()

    val existingBuild = sharedBuilds[app]
    if(existingBuild != null) {
      // There is already a build for given app, wait for its result
      mutex.unlock()
      @Suppress("UNCHECKED_CAST")
      return existingBuild.await()
    }

    if(cacheFunc != null) {
      /* First check if there is already a cached value. This handles the case where a build was removed from
      sharedBuilds before another coroutine could acquire the first lock, causing a recomputation. */
      val cached = cacheFunc(app)
      if(cached != null) {
        mutex.unlock()
        return cached
      }
    }

    // There is no build for given app yet, execute and share it
    val exec: Deferred<UFuncAppData>
    try {
      exec = async(coroutineContext) { execFunc(app) }
      sharedBuilds[app] = exec
    } finally {
      mutex.unlock()
    }

    try {
      return exec.await()
    } finally {
      // Remove shared build after it is finished
      mutex.lock()
      sharedBuilds.remove(app)
      mutex.unlock()
    }
  }


  override fun toString(): String {
    return "CoroutineShare"
  }
}
