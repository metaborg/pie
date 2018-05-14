package mb.pie.runtime.core.impl.cache

import mb.pie.runtime.core.*
import java.util.concurrent.ConcurrentHashMap


class MapCache : Cache {
  private val map = ConcurrentHashMap<UFuncApp, UFuncAppData>()

  override fun set(app: UFuncApp, data: UFuncAppData) {
    map[app] = data
  }

  override fun get(app: UFuncApp): UFuncAppData? {
    return map[app]
  }

  override fun drop() {
    map.clear()
  }


  override fun toString(): String {
    return "MapCache"
  }
}
