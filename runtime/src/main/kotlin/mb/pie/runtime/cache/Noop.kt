package mb.pie.runtime.cache

import mb.pie.api.*

class NoopCache : Cache {
  override fun set(task: UTask, data: UTaskData) = Unit
  override fun get(task: UTask): UTaskData? = null
  override fun drop() = Unit


  override fun toString(): String {
    return "NoopCache"
  }
}
