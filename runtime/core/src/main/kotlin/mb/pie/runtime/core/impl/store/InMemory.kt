package mb.pie.runtime.core.impl.store

import mb.pie.runtime.core.*
import mb.vfs.path.PPath
import java.util.concurrent.ConcurrentHashMap

class InMemoryStore : Store, StoreReadTxn, StoreWriteTxn {
  private val dirty = ConcurrentHashMap.newKeySet<UFuncApp>()
  private val results = ConcurrentHashMap<UFuncApp, UExecRes>()
  private val called = ConcurrentHashMap<UFuncApp, MutableSet<UFuncApp>>()
  private val required = ConcurrentHashMap<PPath, MutableSet<UFuncApp>>()
  private val generated = ConcurrentHashMap<PPath, UFuncApp>()


  override fun readTxn() = this
  override fun writeTxn() = this

  override fun close() {}


  override fun isDirty(app: UFuncApp) = dirty.contains(app)
  override fun setIsDirty(app: UFuncApp, isDirty: Boolean) {
    if(isDirty)
      dirty.add(app)
    else
      dirty.remove(app)
  }

  override fun resultsIn(app: UFuncApp) = results[app]
  override fun setResultsIn(app: UFuncApp, resultsIn: UExecRes) {
    results[app] = resultsIn
  }

  override fun calledBy(app: UFuncApp) = called.getOrPut(app, { ConcurrentHashMap.newKeySet<UFuncApp>() })!!
  override fun setCalledBy(app: UFuncApp, calledBy: UFuncApp) {
    called.getOrPut(app, { ConcurrentHashMap.newKeySet<UFuncApp>() }).add(calledBy)
  }

  override fun requiredBy(path: PPath) = required.getOrPut(path, { ConcurrentHashMap.newKeySet<UFuncApp>() })!!
  override fun setRequiredBy(path: PPath, requiredBy: UFuncApp) {
    required.getOrPut(path, { ConcurrentHashMap.newKeySet<UFuncApp>() }).add(requiredBy)
  }

  override fun generatedBy(path: PPath) = generated[path]
  override fun setGeneratedBy(path: PPath, generatedBy: UFuncApp) {
    generated[path] = generatedBy
  }


  override fun drop() {
    dirty.clear()
    results.clear()
    called.clear()
    required.clear()
    generated.clear()
  }


  override fun toString(): String {
    return "InMemoryStore"
  }
}
