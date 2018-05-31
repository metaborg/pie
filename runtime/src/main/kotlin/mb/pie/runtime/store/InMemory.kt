package mb.pie.runtime.store

import mb.pie.api.*
import mb.pie.vfs.path.PPath
import java.util.concurrent.ConcurrentHashMap

class InMemoryStore : Store, StoreReadTxn, StoreWriteTxn {
  private val inputs = ConcurrentHashMap<TaskKey, In>()
  private val outputs = ConcurrentHashMap<TaskKey, UOutput>()
  private val callReqs = ConcurrentHashMap<TaskKey, ArrayList<TaskReq>>()
  private val callersOf = ConcurrentHashMap<TaskKey, MutableSet<TaskKey>>()
  private val pathReqs = ConcurrentHashMap<TaskKey, ArrayList<FileReq>>()
  private val requireesOf = ConcurrentHashMap<PPath, MutableSet<TaskKey>>()
  private val pathGens = ConcurrentHashMap<TaskKey, ArrayList<FileGen>>()
  private val generatorOf = ConcurrentHashMap<PPath, TaskKey?>()


  override fun readTxn() = this
  override fun writeTxn() = this
  override fun sync() {}
  override fun close() {}


  override fun input(key: TaskKey) = inputs[key]
  override fun setInput(key: TaskKey, input: In) {
    inputs[key] = input
  }

  override fun output(key: TaskKey) = if(!outputs.containsKey(key)) {
    null
  } else {
    val wrapper = outputs[key]!!
    Output(wrapper.output)
  }

  override fun setOutput(key: TaskKey, output: Out) {
    // ConcurrentHashMap does not support null values, so also wrap outputs (which can be null) in an Output object.
    outputs[key] = Output(output)
  }

  override fun taskReqs(key: TaskKey) = callReqs.getOrEmptyList(key)
  override fun callersOf(key: TaskKey): Set<TaskKey> = callersOf.getOrPutSet(key)
  override fun setTaskReqs(key: TaskKey, taskReqs: ArrayList<TaskReq>) {
    // Remove old call requirements
    val oldCallReqs = this.callReqs.remove(key)
    if(oldCallReqs != null) {
      for(callReq in oldCallReqs) {
        callersOf.getOrPutSet(callReq.callee).remove(key)
      }
    }
    // OPTO: diff taskReqs and oldCallReqs, remove/add entries based on diff.
    // Add new call requirements
    this.callReqs[key] = taskReqs
    for(callReq in taskReqs) {
      callersOf.getOrPutSet(callReq.callee).add(key)
    }
  }

  override fun fileReqs(key: TaskKey) = pathReqs.getOrEmptyList(key)
  override fun requireesOf(file: PPath): Set<TaskKey> = requireesOf.getOrPutSet(file)
  override fun setFileReqs(key: TaskKey, fileReqs: ArrayList<FileReq>) {
    // Remove old file requirements
    val oldPathReqs = this.pathReqs.remove(key)
    if(oldPathReqs != null) {
      for(pathReq in oldPathReqs) {
        requireesOf.getOrPutSet(pathReq.file).remove(key)
      }
    }
    // OPTO: diff fileReqs and oldPathReqs, remove/add entries based on diff.
    // Add new call requirements
    this.pathReqs[key] = fileReqs
    for(pathReq in fileReqs) {
      requireesOf.getOrPutSet(pathReq.file).add(key)
    }
  }

  override fun fileGens(key: TaskKey) = pathGens.getOrEmptyList(key)
  override fun generatorOf(file: PPath): TaskKey? = generatorOf[file]
  override fun setFileGens(key: TaskKey, fileGens: ArrayList<FileGen>) {
    // Remove old file generators
    val oldPathGens = this.pathGens.remove(key)
    if(oldPathGens != null) {
      for(pathGen in oldPathGens) {
        generatorOf.remove(pathGen.file)
      }
    }
    // OPTO: diff fileGens and oldPathGens, remove/add entries based on diff.
    // Add new file generators
    this.pathGens[key] = fileGens
    for(pathGen in fileGens) {
      generatorOf[pathGen.file] = key
    }
  }

  override fun data(key: TaskKey): UTaskData? {
    val input = input(key) ?: return null
    val output = output(key) ?: return null
    val callReqs = taskReqs(key)
    val pathReqs = fileReqs(key)
    val pathGens = fileGens(key)
    return TaskData(input, output.output, callReqs, pathReqs, pathGens)
  }

  override fun setData(key: TaskKey, data: UTaskData) {
    val (input, output, callReqs, pathReqs, pathGens) = data
    setInput(key, input)
    setOutput(key, output)
    setTaskReqs(key, callReqs)
    setFileReqs(key, pathReqs)
    setFileGens(key, pathGens)
  }


  override fun numSourceFiles(): Int {
    val requiredFiles = pathReqs.values.flatMap { it.map { it.file } }.toHashSet()
    var numSourceFiles = 0
    for(file in requiredFiles) {
      if(!generatorOf.containsKey(file)) {
        ++numSourceFiles
      }
    }
    return numSourceFiles
  }


  override fun drop() {
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
