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
  override fun sync() {}

  override fun close() {}


  override fun isDirty(app: UFuncApp) = dirty.contains(app)
  override fun setIsDirty(app: UFuncApp, isDirty: Boolean) {
    if(isDirty)
      dirty.add(app)
    else
      dirty.remove(app)
  }

  override fun resultOf(app: UFuncApp) = results[app]
  override fun setResultOf(app: UFuncApp, result: UExecRes) {
    results[app] = result
  }

  override fun callersOf(callee: UFuncApp) = called.getOrPut(callee, { ConcurrentHashMap.newKeySet<UFuncApp>() })!!
  override fun setCallerOf(caller: UFuncApp, callee: UFuncApp) {
    called.getOrPut(callee, { ConcurrentHashMap.newKeySet<UFuncApp>() }).add(caller)
  }

  override fun requireesOf(path: PPath) = required.getOrPut(path, { ConcurrentHashMap.newKeySet<UFuncApp>() })!!
  override fun setRequireeOf(requiree: UFuncApp, path: PPath) {
    required.getOrPut(path, { ConcurrentHashMap.newKeySet<UFuncApp>() }).add(requiree)
  }

  override fun generatorOf(path: PPath) = generated[path]
  override fun setGeneratorOf(generator: UFuncApp, path: PPath) {
    generated[path] = generator
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
