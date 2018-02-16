package mb.pie.runtime.core


interface Cache {
  operator fun set(app: UFuncApp, data: UFuncAppData)
  operator fun get(app: UFuncApp): UFuncAppData?
  fun drop()
}
