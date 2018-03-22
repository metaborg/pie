package mb.pie.runtime.core.impl.store

import mb.pie.runtime.core.*
import mb.pie.runtime.core.impl.*
import mb.vfs.path.PPath

/**
 * A build store that does not store anything and always returns null. For debugging or benchmarking purposes only.
 * DO NOT USE in production, as it makes the build algorithm inconsistent.
 */
class NoopStore : Store, StoreReadTxn, StoreWriteTxn {
  override fun readTxn() = this
  override fun writeTxn() = this
  override fun sync() {}
  override fun close() {}

  override fun dirty(app: UFuncApp): Boolean = false
  override fun output(app: UFuncApp): Output<Out>? = null
  override fun callReqs(app: UFuncApp): List<CallReq> = listOf()
  override fun callersOf(app: UFuncApp): Set<UFuncApp> = setOf()
  override fun pathReqs(app: UFuncApp): List<PathReq> = listOf()
  override fun requireesOf(path: PPath): Set<UFuncApp> = setOf()
  override fun pathGens(app: UFuncApp): List<PathGen> = listOf()
  override fun generatorOf(path: PPath): UFuncApp? = null
  override fun data(app: UFuncApp): UFuncAppData? = null

  override fun setDirty(app: UFuncApp, isDirty: Boolean) {}
  override fun setOutput(app: UFuncApp, output: Out) {}
  override fun setCallReqs(app: UFuncApp, callReqs: ArrayList<CallReq>) {}
  override fun setPathReqs(app: UFuncApp, pathReqs: ArrayList<PathReq>) {}
  override fun setPathGens(app: UFuncApp, pathGens: ArrayList<PathGen>) {}
  override fun setData(app: UFuncApp, data: UFuncAppData) {}
  override fun drop() {}

  override fun toString() = "NoopStore"
}