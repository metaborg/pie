package mb.pie.runtime.core.impl.share

import mb.pie.runtime.core.*

class NonSharingShare : Share {
  override fun reuseOrCreate(app: UFuncApp, cacheFunc: (UFuncApp) -> UFuncAppData?, execFunc: (UFuncApp) -> UFuncAppData): UFuncAppData {
    return cacheFunc(app) ?: execFunc(app)
  }

  override fun reuseOrCreate(app: UFuncApp, execFunc: (UFuncApp) -> UFuncAppData): UFuncAppData {
    return execFunc(app)
  }


  override fun toString() = "NonSharingShare"
}