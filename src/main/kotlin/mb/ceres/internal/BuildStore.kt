package mb.ceres.internal

import mb.ceres.CPath
import mb.ceres.UBuildApp

interface BuildStore {
  operator fun set(app: UBuildApp, res: UBuildRes)
  operator fun set(path: CPath, res: UBuildRes)
  operator fun get(app: UBuildApp): UBuildRes?
  operator fun get(path: CPath): UBuildRes?
  fun remove(path: CPath, res: UBuildRes)
}

class InMemoryBuildStore(produces: Map<UBuildApp, UBuildRes> = emptyMap(), generates: Map<CPath, UBuildRes> = emptyMap()) : BuildStore {
  val produces = produces.toMutableMap()
  val generates = generates.toMutableMap()

  override fun set(app: UBuildApp, res: UBuildRes) {
    produces[app] = res
  }

  override fun set(path: CPath, res: UBuildRes) {
    generates[path] = res
  }

  override fun get(app: UBuildApp): UBuildRes? {
    return produces[app]
  }

  override fun get(path: CPath): UBuildRes? {
    return generates[path]
  }

  override fun remove(path: CPath, res: UBuildRes) {
    generates.remove(path, res)
  }
}