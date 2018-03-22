package mb.pie.runtime.core


interface Share {
  fun reuseOrCreate(app: UFuncApp, cacheFunc: (UFuncApp) -> UFuncAppData?, execFunc: (UFuncApp) -> UFuncAppData): UFuncAppData
  fun reuseOrCreate(app: UFuncApp, execFunc: (UFuncApp) -> UFuncAppData): UFuncAppData
}
