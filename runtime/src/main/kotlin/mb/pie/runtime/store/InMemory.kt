package mb.pie.runtime.store

import mb.pie.api.*
import mb.pie.vfs.path.PPath
import java.util.concurrent.ConcurrentHashMap

class InMemoryStore : Store, StoreReadTxn, StoreWriteTxn {
  private val outputs = ConcurrentHashMap<UTask, UOutput>()
  private val callReqs = ConcurrentHashMap<UTask, ArrayList<TaskReq>>()
  private val callersOf = ConcurrentHashMap<UTask, MutableSet<UTask>>()
  private val pathReqs = ConcurrentHashMap<UTask, ArrayList<FileReq>>()
  private val requireesOf = ConcurrentHashMap<PPath, MutableSet<UTask>>()
  private val pathGens = ConcurrentHashMap<UTask, ArrayList<FileGen>>()
  private val generatorOf = ConcurrentHashMap<PPath, UTask?>()


  override fun readTxn() = this
  override fun writeTxn() = this
  override fun sync() {}
  override fun close() {}


  override fun output(task: UTask) = if(!outputs.containsKey(task)) {
    null
  } else {
    val wrapper = outputs[task]!!
    Output(wrapper.output)
  }

  override fun setOutput(task: UTask, output: Out) {
    // ConcurrentHashMap does not support null values, so also wrap outputs (which can be null) in an Output object.
    outputs[task] = Output(output)
  }

  override fun taskReqs(task: UTask) = callReqs.getOrEmptyList(task)
  override fun callersOf(task: UTask) = callersOf.getOrPutSet(task)
  override fun setTaskReqs(task: UTask, taskReqs: ArrayList<TaskReq>) {
    // Remove old call requirements
    val oldCallReqs = this.callReqs.remove(task)
    if(oldCallReqs != null) {
      for(callReq in oldCallReqs) {
        callersOf.getOrPutSet(callReq.callee).remove(task)
      }
    }
    // OPTO: diff taskReqs and oldCallReqs, remove/add entries based on diff.
    // Add new call requirements
    this.callReqs[task] = taskReqs
    for(callReq in taskReqs) {
      callersOf.getOrPutSet(callReq.callee).add(task)
    }
  }

  override fun fileReqs(task: UTask) = pathReqs.getOrEmptyList(task)
  override fun requireesOf(file: PPath) = requireesOf.getOrPutSet(file)
  override fun setFileReqs(task: UTask, fileReqs: ArrayList<FileReq>) {
    // Remove old file requirements
    val oldPathReqs = this.pathReqs.remove(task)
    if(oldPathReqs != null) {
      for(pathReq in oldPathReqs) {
        requireesOf.getOrPutSet(pathReq.file).remove(task)
      }
    }
    // OPTO: diff fileReqs and oldPathReqs, remove/add entries based on diff.
    // Add new call requirements
    this.pathReqs[task] = fileReqs
    for(pathReq in fileReqs) {
      requireesOf.getOrPutSet(pathReq.file).add(task)
    }
  }

  override fun fileGens(task: UTask) = pathGens.getOrEmptyList(task)
  override fun generatorOf(file: PPath) = generatorOf[file]
  override fun setFileGens(task: UTask, fileGens: ArrayList<FileGen>) {
    // Remove old file generators
    val oldPathGens = this.pathGens.remove(task)
    if(oldPathGens != null) {
      for(pathGen in oldPathGens) {
        generatorOf.remove(pathGen.file)
      }
    }
    // OPTO: diff fileGens and oldPathGens, remove/add entries based on diff.
    // Add new file generators
    this.pathGens[task] = fileGens
    for(pathGen in fileGens) {
      generatorOf[pathGen.file] = task
    }
  }

  override fun data(task: UTask): UTaskData? {
    val output = output(task) ?: return null
    val callReqs = taskReqs(task)
    val pathReqs = fileReqs(task)
    val pathGens = fileGens(task)
    return TaskData(output.output, callReqs, pathReqs, pathGens)
  }

  override fun setData(task: UTask, data: UTaskData) {
    val (output, callReqs, pathReqs, pathGens) = data
    setOutput(task, output)
    setTaskReqs(task, callReqs)
    setFileReqs(task, pathReqs)
    setFileGens(task, pathGens)
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
