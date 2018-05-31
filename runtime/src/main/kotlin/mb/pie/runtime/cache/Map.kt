package mb.pie.runtime.cache

import mb.pie.api.*
import java.util.concurrent.ConcurrentHashMap

class MapCache : Cache {
  private val map = ConcurrentHashMap<TaskKey, UTaskData>()

  override fun set(key: TaskKey, data: UTaskData) {
    map[key] = data
  }

  override fun get(key: TaskKey): UTaskData? {
    return map[key]
  }

  override fun drop() {
    map.clear()
  }


  override fun toString(): String {
    return "MapCache"
  }
}
