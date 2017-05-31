package mb.ceres.internal

import kotlinx.coroutines.experimental.Deferred
import mb.ceres.UBuildApp
import java.util.concurrent.ConcurrentHashMap

interface BuildShare {
  operator fun set(app: UBuildApp, deferred: Deferred<UBuildRes>)
  operator fun get(app: UBuildApp): Deferred<UBuildRes>?
  fun remove(app: UBuildApp)
}

class BuildShareImpl : BuildShare {
  private val sharedBuilds = ConcurrentHashMap<UBuildApp, Deferred<UBuildRes>>()

  override operator fun set(app: UBuildApp, deferred: Deferred<UBuildRes>) {
    sharedBuilds[app] = deferred
  }

  override operator fun get(app: UBuildApp): Deferred<UBuildRes>? {
    return sharedBuilds[app]
  }

  override fun remove(app: UBuildApp) {
    sharedBuilds.remove(app)
  }
}