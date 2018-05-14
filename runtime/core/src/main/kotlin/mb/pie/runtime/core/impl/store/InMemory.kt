package mb.pie.runtime.core.impl.store

import mb.pie.runtime.core.*
import mb.pie.runtime.core.impl.*
import mb.vfs.path.PPath
import java.util.concurrent.ConcurrentHashMap

class InMemoryStore : Store, StoreReadTxn, StoreWriteTxn {
  private val dirty = ConcurrentHashMap.newKeySet<UFuncApp>()
  private val outputs = ConcurrentHashMap<UFuncApp, UOutput>()
  private val callReqs = ConcurrentHashMap<UFuncApp, ArrayList<CallReq>>()
  private val callersOf = ConcurrentHashMap<UFuncApp, MutableSet<UFuncApp>>()
  private val pathReqs = ConcurrentHashMap<UFuncApp, ArrayList<PathReq>>()
  private val requireesOf = ConcurrentHashMap<PPath, MutableSet<UFuncApp>>()
  private val pathGens = ConcurrentHashMap<UFuncApp, ArrayList<PathGen>>()
  private val generatorOf = ConcurrentHashMap<PPath, UFuncApp?>()


  override fun readTxn() = this
  override fun writeTxn() = this
  override fun sync() {}
  override fun close() {}

  override fun dirty(app: UFuncApp) = dirty.contains(app)
  override fun setDirty(app: UFuncApp, isDirty: Boolean) {
    if(isDirty)
      dirty.add(app)
    else
      dirty.remove(app)
  }

  override fun output(app: UFuncApp) = if(!outputs.containsKey(app)) {
    null
  } else {
    val wrapper = outputs[app]!!
    Output(wrapper.output)
  }

  override fun setOutput(app: UFuncApp, output: Out) {
    // ConcurrentHashMap does not support null values, so also wrap outputs (which can be null) in an Output object.
    outputs[app] = Output(output)
  }

  override fun callReqs(app: UFuncApp) = callReqs.getOrEmptyList(app)
  override fun callersOf(app: UFuncApp) = callersOf.getOrPutSet(app)
  override fun setCallReqs(app: UFuncApp, callReqs: ArrayList<CallReq>) {
    // Remove old call requirements
    val oldCallReqs = this.callReqs.remove(app)
    if(oldCallReqs != null) {
      for(callReq in oldCallReqs) {
        callersOf.getOrPutSet(callReq.callee).remove(app)
      }
    }
    // OPTO: diff callReqs and oldCallReqs, remove/add entries based on diff.
    // Add new call requirements
    this.callReqs[app] = callReqs
    for(callReq in callReqs) {
      callersOf.getOrPutSet(callReq.callee).add(app)
    }
  }

  override fun pathReqs(app: UFuncApp) = pathReqs.getOrEmptyList(app)
  override fun requireesOf(path: PPath) = requireesOf.getOrPutSet(path)
  override fun setPathReqs(app: UFuncApp, pathReqs: ArrayList<PathReq>) {
    // Remove old path requirements
    val oldPathReqs = this.pathReqs.remove(app)
    if(oldPathReqs != null) {
      for(pathReq in oldPathReqs) {
        requireesOf.getOrPutSet(pathReq.path).remove(app)
      }
    }
    // OPTO: diff pathReqs and oldPathReqs, remove/add entries based on diff.
    // Add new call requirements
    this.pathReqs[app] = pathReqs
    for(pathReq in pathReqs) {
      requireesOf.getOrPutSet(pathReq.path).add(app)
    }
  }

  override fun pathGens(app: UFuncApp) = pathGens.getOrEmptyList(app)
  override fun generatorOf(path: PPath) = generatorOf[path]
  override fun setPathGens(app: UFuncApp, pathGens: ArrayList<PathGen>) {
    // Remove old path generators
    val oldPathGens = this.pathGens.remove(app)
    if(oldPathGens != null) {
      for(pathGen in oldPathGens) {
        generatorOf.remove(pathGen.path)
      }
    }
    // OPTO: diff pathGens and oldPathGens, remove/add entries based on diff.
    // Add new path generators
    this.pathGens[app] = pathGens
    for(pathGen in pathGens) {
      generatorOf[pathGen.path] = app
    }
  }

  override fun data(app: UFuncApp): UFuncAppData? {
    val output = output(app) ?: return null
    val callReqs = callReqs(app)
    val pathReqs = pathReqs(app)
    val pathGens = pathGens(app)
    return FuncAppData(output.output, callReqs, pathReqs, pathGens)
  }

  override fun setData(app: UFuncApp, data: UFuncAppData) {
    val (output, callReqs, pathReqs, pathGens) = data
    setOutput(app, output)
    setCallReqs(app, callReqs)
    setPathReqs(app, pathReqs)
    setPathGens(app, pathGens)
  }


  override fun numSourceFiles(): Int {
    val requiredFiles = pathReqs.values.flatMap { it.map { it.path } }.toHashSet()
    var numSourceFiles = 0
    for(file in requiredFiles) {
      if(!generatorOf.containsKey(file)) {
        ++numSourceFiles
      }
    }
    return numSourceFiles
  }


  override fun drop() {
    dirty.clear()
    outputs.clear()
    callReqs.clear()
    callersOf.clear()
    pathReqs.clear()
    requireesOf.clear()
    pathGens.clear()
    generatorOf.clear()
  }


  override fun toString(): String {
    return "InMemoryStore"
  }
}

private fun <K, V> ConcurrentHashMap<K, MutableSet<V>>.getOrPutSet(key: K) = this.getOrPut(key, { ConcurrentHashMap.newKeySet<V>() })!!
private fun <K, V> ConcurrentHashMap<K, ArrayList<V>>.getOrEmptyList(key: K) = this.getOrElse(key) { arrayListOf() }
