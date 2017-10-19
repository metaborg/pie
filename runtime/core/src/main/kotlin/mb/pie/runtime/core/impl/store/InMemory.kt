package mb.pie.runtime.core.impl.store

import mb.pie.runtime.core.*
import mb.vfs.path.PPath
import java.util.concurrent.ConcurrentHashMap

class InMemoryStore : Store, StoreReadTxn, StoreWriteTxn {
  private val produces = ConcurrentHashMap<UFuncApp, UExecRes>()
  private val generatedBy = ConcurrentHashMap<PPath, UFuncApp>()
  private val requiredBy = ConcurrentHashMap<PPath, UFuncApp>()


  override fun readTxn(): StoreReadTxn {
    return this
  }

  override fun writeTxn(): StoreWriteTxn {
    return this
  }

  override fun close() {}


  override fun setProduces(app: UFuncApp, res: UExecRes) {
    produces[app] = res
  }

  override fun produces(app: UFuncApp): UExecRes? {
    return produces[app]
  }


  override fun setGeneratedBy(path: PPath, res: UFuncApp) {
    generatedBy[path] = res
  }

  override fun generatedBy(path: PPath): UFuncApp? {
    return generatedBy[path]
  }


  override fun setRequiredBy(path: PPath, res: UFuncApp) {
    requiredBy[path] = res
  }

  override fun requiredBy(path: PPath): UFuncApp? {
    return requiredBy[path]
  }


  override fun drop() {
    produces.clear()
    generatedBy.clear()
    requiredBy.clear()
  }


  override fun toString(): String {
    return "InMemoryStore"
  }
}
