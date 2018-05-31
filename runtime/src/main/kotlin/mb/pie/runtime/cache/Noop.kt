package mb.pie.runtime.cache

import mb.pie.api.*

class NoopCache : Cache {
  override fun set(key: TaskKey, data: UTaskData) = Unit
  override fun get(key: TaskKey): UTaskData? = null
  override fun drop() = Unit


  override fun toString(): String {
    return "NoopCache"
  }
}
