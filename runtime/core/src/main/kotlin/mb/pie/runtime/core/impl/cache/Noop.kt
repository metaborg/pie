package mb.pie.runtime.core.impl.cache

import mb.pie.runtime.core.*

class NoopCache : Cache {
  override fun set(app: UFuncApp, data: UFuncAppData) = Unit
  override fun get(app: UFuncApp): UFuncAppData? = null
  override fun drop() = Unit


  override fun toString(): String {
    return "NoopCache"
  }
}