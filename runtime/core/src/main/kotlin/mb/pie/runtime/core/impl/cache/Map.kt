package mb.pie.runtime.core.impl.cache

import mb.pie.runtime.core.*
import java.util.concurrent.ConcurrentHashMap

class MapBuildCache : BuildCache {
  private val map = ConcurrentHashMap<UBuildApp, UBuildRes>()

  override fun set(app: UBuildApp, res: UBuildRes) {
    map[app] = res
  }

  override fun get(app: UBuildApp): UBuildRes? {
    return map[app]
  }

  override fun drop() {
    map.clear()
  }


  override fun toString(): String {
    return "MapBuildCache"
  }
}