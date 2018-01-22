package mb.pie.runtime.core.impl.store

import mb.pie.runtime.core.*
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


  override fun isDirty(app: UFuncApp) = false
  override fun setIsDirty(app: UFuncApp, isDirty: Boolean) {}

  override fun resultOf(app: UFuncApp) = null
  override fun setResultOf(app: UFuncApp, result: UExecRes) {}

  override fun callersOf(callee: UFuncApp) = setOf<UFuncApp>()
  override fun setCallerOf(caller: UFuncApp, callee: UFuncApp) {}

  override fun requireesOf(path: PPath) = setOf<UFuncApp>()
  override fun setRequireeOf(requiree: UFuncApp, path: PPath) {}

  override fun generatorOf(path: PPath) = null
  override fun setGeneratorOf(generator: UFuncApp, path: PPath) {}


  override fun drop() {}


  override fun toString() = "NoopStore"
}