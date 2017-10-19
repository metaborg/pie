package mb.pie.runtime.core.impl.store

import mb.pie.runtime.core.*
import mb.vfs.path.PPath
import java.util.concurrent.ConcurrentHashMap

class InMemoryBuildStore : BuildStore, BuildStoreReadTxn, BuildStoreWriteTxn {
  private val produces = ConcurrentHashMap<UBuildApp, UBuildRes>()
  private val generatedBy = ConcurrentHashMap<PPath, UBuildApp>()
  private val requiredBy = ConcurrentHashMap<PPath, UBuildApp>()


  override fun readTxn(): BuildStoreReadTxn {
    return this
  }

  override fun writeTxn(): BuildStoreWriteTxn {
    return this
  }

  override fun close() {}


  override fun setProduces(app: UBuildApp, res: UBuildRes) {
    produces[app] = res
  }

  override fun produces(app: UBuildApp): UBuildRes? {
    return produces[app]
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
    produces.clear()
    generatedBy.clear()
    requiredBy.clear()
  }


  override fun toString(): String {
    return "InMemoryBuildStore"
  }
}
