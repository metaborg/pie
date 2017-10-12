package mb.pie.runtime.core.impl.cache

import mb.pie.runtime.core.*

class NoopBuildCache : BuildCache {
  override fun set(app: UBuildApp, res: UBuildRes) = Unit
  override fun get(app: UBuildApp): UBuildRes? = null
  override fun drop() = Unit


  override fun toString(): String {
    return "NoopBuildCache"
  }
}