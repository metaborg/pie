package mb.pie.runtime.core.impl.cache

import mb.pie.runtime.core.*

class NoopCache : Cache {
  override fun set(app: UFuncApp, res: UExecRes) = Unit
  override fun get(app: UFuncApp): UExecRes? = null
  override fun drop() = Unit


  override fun toString(): String {
    return "NoopCache"
  }
}