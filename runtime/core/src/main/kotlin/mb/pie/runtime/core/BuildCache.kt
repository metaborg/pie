package mb.pie.runtime.core

interface BuildCache {
  operator fun set(app: UBuildApp, res: UBuildRes)
  operator fun get(app: UBuildApp): UBuildRes?
  fun drop()
}
