package mb.pie.runtime.core.impl

import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import kotlinx.coroutines.experimental.sync.Mutex
import mb.pie.runtime.core.*

interface BuildShare {
  fun <I : In, O : Out> reuseOrCreate(app: BuildApp<I, O>, cacheFunc: (BuildApp<I, O>) -> BuildRes<I, O>?, buildFunc: (BuildApp<I, O>) -> BuildRes<I, O>): BuildRes<I, O>
  fun <I : In, O : Out> reuseOrCreate(app: BuildApp<I, O>, buildFunc: (BuildApp<I, O>) -> BuildRes<I, O>): BuildRes<I, O>
}

class BuildShareImpl : BuildShare {
  private val sharedBuilds = mutableMapOf<UBuildApp, Deferred<UBuildRes>>()
  private val mutex = Mutex()


  override fun <I : In, O : Out> reuseOrCreate(app: BuildApp<I, O>, cacheFunc: (BuildApp<I, O>) -> BuildRes<I, O>?, buildFunc: (BuildApp<I, O>) -> BuildRes<I, O>): BuildRes<I, O> {
    return runBlocking { getResult(app, cacheFunc, buildFunc) }
  }

  override fun <I : In, O : Out> reuseOrCreate(app: BuildApp<I, O>, buildFunc: (BuildApp<I, O>) -> BuildRes<I, O>): BuildRes<I, O> {
    return runBlocking { getResult(app, null, buildFunc) }
  }


  private suspend fun <I : In, O : Out> CoroutineScope.getResult(app: BuildApp<I, O>, cacheFunc: ((BuildApp<I, O>) -> BuildRes<I, O>?)?, buildFunc: (BuildApp<I, O>) -> BuildRes<I, O>): BuildRes<I, O> {
    mutex.lock()

    val existingBuild = sharedBuilds[app]
    if (existingBuild != null) {
      // There is already a build for given app, wait for its result
      mutex.unlock()
      @Suppress("UNCHECKED_CAST")
      return existingBuild.await() as BuildRes<I, O>
    }

    if (cacheFunc != null) {
      /* First check if there is already a cached value. This handles the case where a build was removed from
      sharedBuilds before another coroutine could acquire the first lock, causing a recomputation. */
      val cached = cacheFunc(app)
      if (cached != null) {
        mutex.unlock()
        return cached
      }
    }

    // There is no build for given app yet, create a new build and share it
    val build: Deferred<BuildRes<I, O>>
    try {
      build = async(context) { buildFunc(app) }
      sharedBuilds[app] = build
    } finally {
      mutex.unlock()
    }

    try {
      return build.await()
    } finally {
      // Remove shared build after it is finished
      mutex.lock()
      sharedBuilds.remove(app)
      mutex.unlock()
    }
  }


  override fun toString(): String {
    return "BuildShare"
  }
}