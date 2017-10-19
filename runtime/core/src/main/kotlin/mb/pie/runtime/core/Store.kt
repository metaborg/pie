package mb.pie.runtime.core

import mb.vfs.path.PPath


interface Store : AutoCloseable {
  fun readTxn(): StoreReadTxn
  fun writeTxn(): StoreWriteTxn
}

interface StoreTxn : AutoCloseable {
  override fun close()
}

interface StoreReadTxn : StoreTxn {
  fun produces(app: UFuncApp): UExecRes?

  fun generatedBy(path: PPath): UFuncApp?

  fun requiredBy(path: PPath): UFuncApp?
}

interface StoreWriteTxn : StoreReadTxn {
  fun setProduces(app: UFuncApp, res: UExecRes)

  fun setGeneratedBy(path: PPath, res: UFuncApp)

  fun setRequiredBy(path: PPath, res: UFuncApp)

  fun drop()
}
