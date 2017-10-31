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
  override fun close() {}


  override fun isDirty(app: UFuncApp) = false
  override fun setIsDirty(app: UFuncApp, isDirty: Boolean) {}

  override fun resultsIn(app: UFuncApp) = null
  override fun setResultsIn(app: UFuncApp, resultsIn: UExecRes) {}

  override fun calledBy(app: UFuncApp) = setOf<UFuncApp>()
  override fun setCalledBy(app: UFuncApp, calledBy: UFuncApp) {}

  override fun requiredBy(path: PPath) = setOf<UFuncApp>()
  override fun setRequiredBy(path: PPath, requiredBy: UFuncApp) {}

  override fun generatedBy(path: PPath) = null
  override fun setGeneratedBy(path: PPath, generatedBy: UFuncApp) {}


  override fun drop() {}


  override fun toString() = "NoopStore"
}