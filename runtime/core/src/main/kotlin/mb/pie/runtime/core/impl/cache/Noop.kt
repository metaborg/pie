package mb.pie.runtime.core.impl.cache

import mb.pie.runtime.core.*


class NoopCache : Cache {
  override fun set(task: UTask, data: UTaskData) = Unit
  override fun get(task: UTask): UTaskData? = null
  override fun drop() = Unit


  override fun toString(): String {
    return "NoopCache"
  }
}
