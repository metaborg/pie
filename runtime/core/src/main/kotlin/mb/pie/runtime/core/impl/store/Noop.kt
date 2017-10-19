package mb.pie.runtime.core.impl.store

import mb.pie.runtime.core.*
import mb.vfs.path.PPath

/**
 * A build store that does not store anything and always returns null. For debugging or benchmarking purposes only.
 * DO NOT USE in production, as it makes the build algorithm inconsistent.
 */
class NoopBuildStore : BuildStore, BuildStoreReadTxn, BuildStoreWriteTxn {
  override fun readTxn(): BuildStoreReadTxn {
    return this
  }

  override fun writeTxn(): BuildStoreWriteTxn {
    return this
  }

  override fun close() {}


  override fun setProduces(app: UBuildApp, res: UBuildRes) {}
  override fun produces(app: UBuildApp): UBuildRes? {
    return null
  }

  override fun setGeneratedBy(path: PPath, res: UBuildApp) {}
  override fun generatedBy(path: PPath): UBuildApp? {
    return null
  }

  override fun setRequiredBy(path: PPath, res: UBuildApp) {}
  override fun requiredBy(path: PPath): UBuildApp? {
    return null
  }


  override fun drop() {}


  override fun toString(): String {
    return "NoopBuildStore"
  }
}