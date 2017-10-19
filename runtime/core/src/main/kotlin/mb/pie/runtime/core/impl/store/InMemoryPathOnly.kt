package mb.pie.runtime.core.impl.store

import mb.pie.runtime.core.*
import mb.vfs.path.PPath
import java.util.concurrent.ConcurrentHashMap

/**
 * A build store that stored generated and required path dependencies in concurrent hash maps. For debugging or benchmarking purposes only.
 * Does not store the result of builds in a hash map, and therefore does not provide incrementality between changes in build outputs.
 */
class InMemoryPathOnlyStore : Store, StoreReadTxn, StoreWriteTxn {
  private val generatedBy = ConcurrentHashMap<PPath, UFuncApp>()
  private val requiredBy = ConcurrentHashMap<PPath, UFuncApp>()


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
    generatedBy.clear()
    requiredBy.clear()
  }


  override fun toString(): String {
    return "InMemoryPathOnlyStore"
  }
}