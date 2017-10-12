package mb.pie.runtime.core

import mb.vfs.path.PPath

interface BuildStore : AutoCloseable {
  fun readTxn(): BuildStoreReadTxn
  fun writeTxn(): BuildStoreWriteTxn
}

interface BuildStoreTxn : AutoCloseable {
  override fun close()
}

interface BuildStoreReadTxn : BuildStoreTxn {
  fun produces(app: UBuildApp): UBuildRes?

  fun generatedBy(path: PPath): UBuildApp?

  fun requiredBy(path: PPath): UBuildApp?
}

interface BuildStoreWriteTxn : BuildStoreReadTxn {
  fun setProduces(app: UBuildApp, res: UBuildRes)

  fun setGeneratedBy(path: PPath, res: UBuildApp)

  fun setRequiredBy(path: PPath, res: UBuildApp)

  fun drop()
}
