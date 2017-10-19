package mb.pie.runtime.core.impl.cache

import mb.pie.runtime.core.*
import java.util.concurrent.ConcurrentHashMap

class MapCache : Cache {
  private val map = ConcurrentHashMap<UFuncApp, UExecRes>()

  override fun set(app: UFuncApp, res: UExecRes) {
    map[app] = res
  }

  override fun get(app: UFuncApp): UExecRes? {
    return map[app]
  }

  override fun drop() {
    map.clear()
  }


  override fun toString(): String {
    return "MapCache"
  }
}