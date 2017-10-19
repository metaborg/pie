package mb.pie.runtime.core.impl.store

import mb.pie.runtime.core.*
import mb.vfs.path.PPath

/**
 * A build store that does not store anything and always returns null. For debugging or benchmarking purposes only.
 * DO NOT USE in production, as it makes the build algorithm inconsistent.
 */
class NoopStore : Store, StoreReadTxn, StoreWriteTxn {
  override fun readTxn(): StoreReadTxn {
    return this
  }

  override fun writeTxn(): StoreWriteTxn {
    return this
  }

  override fun close() {}


  override fun setProduces(app: UFuncApp, res: UExecRes) {}
  override fun produces(app: UFuncApp): UExecRes? {
    return null
  }

  override fun setGeneratedBy(path: PPath, res: UFuncApp) {}
  override fun generatedBy(path: PPath): UFuncApp? {
    return null
  }

  override fun setRequiredBy(path: PPath, res: UFuncApp) {}
  override fun requiredBy(path: PPath): UFuncApp? {
    return null
  }


  override fun drop() {}


  override fun toString(): String {
    return "NoopStore"
  }
}