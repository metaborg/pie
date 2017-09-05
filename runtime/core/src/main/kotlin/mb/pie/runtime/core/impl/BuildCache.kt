package mb.pie.runtime.core.impl

import mb.pie.runtime.core.UBuildApp
import mb.pie.runtime.core.UBuildRes
import java.util.concurrent.ConcurrentHashMap

interface BuildCache {
  operator fun set(app: UBuildApp, res: UBuildRes)
  operator fun get(app: UBuildApp): UBuildRes?
  fun drop()
}


class NoBuildCache : BuildCache {
  override fun set(app: UBuildApp, res: UBuildRes) = Unit
  override fun get(app: UBuildApp): UBuildRes? = null
  override fun drop() = Unit


  override fun toString(): String {
    return "NoBuildCache"
  }
}

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