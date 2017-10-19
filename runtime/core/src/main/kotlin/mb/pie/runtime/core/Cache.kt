package mb.pie.runtime.core


interface Cache {
  operator fun set(app: UFuncApp, res: UExecRes)
  operator fun get(app: UFuncApp): UExecRes?
  fun drop()
}
