package mb.pie.runtime.core.impl.store

import mb.pie.runtime.core.*
import mb.vfs.path.PPath
import java.util.concurrent.ConcurrentHashMap

/**
 * A build store that stored generated and required path dependencies in concurrent hash maps. For debugging or benchmarking purposes only.
 * Does not store the result of builds in a hash map, and therefore does not provide incrementality between changes in build outputs.
 */
class InMemoryPathOnlyBuildStore : BuildStore, BuildStoreReadTxn, BuildStoreWriteTxn {
  private val generatedBy = ConcurrentHashMap<PPath, UBuildApp>()
  private val requiredBy = ConcurrentHashMap<PPath, UBuildApp>()


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


  override fun setGeneratedBy(path: PPath, res: UBuildApp) {
    generatedBy[path] = res
  }

  override fun generatedBy(path: PPath): UBuildApp? {
    return generatedBy[path]
  }


  override fun setRequiredBy(path: PPath, res: UBuildApp) {
    requiredBy[path] = res
  }

  override fun requiredBy(path: PPath): UBuildApp? {
    return requiredBy[path]
  }


  override fun drop() {
    generatedBy.clear()
    requiredBy.clear()
  }


  override fun toString(): String {
    return "InMemoryPathOnlyBuildStore"
  }
}