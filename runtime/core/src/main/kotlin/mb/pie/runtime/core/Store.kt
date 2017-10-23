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
  fun isDirty(app: UFuncApp): Boolean

  fun resultsIn(app: UFuncApp): UExecRes?

  fun calledBy(app: UFuncApp): Set<UFuncApp>

  fun requiredBy(path: PPath): Set<UFuncApp>

  fun generatedBy(path: PPath): UFuncApp?
}

interface StoreWriteTxn : StoreReadTxn {
  fun setIsDirty(app: UFuncApp, isDirty: Boolean)

  fun setResultsIn(app: UFuncApp, resultsIn: UExecRes)

  fun setCalledBy(app: UFuncApp, calledBy: UFuncApp)

  fun setRequiredBy(path: PPath, requiredBy: UFuncApp)

  fun setGeneratedBy(path: PPath, generatedBy: UFuncApp)

  fun drop()
}
