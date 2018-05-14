package mb.pie.runtime.core.impl.cache

import mb.pie.runtime.core.*
import java.util.concurrent.ConcurrentHashMap


class MapCache : Cache {
  private val map = ConcurrentHashMap<UTask, UTaskData>()

  override fun set(task: UTask, data: UTaskData) {
    map[task] = data
  }

  override fun get(task: UTask): UTaskData? {
    return map[task]
  }

  override fun drop() {
    map.clear()
  }


  override fun toString(): String {
    return "MapCache"
  }
}
