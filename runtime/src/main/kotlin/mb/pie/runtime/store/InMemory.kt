package mb.pie.runtime.store

import mb.pie.api.*
import java.util.concurrent.ConcurrentHashMap

class InMemoryStore : Store, StoreReadTxn, StoreWriteTxn {
  private val inputs = ConcurrentHashMap<TaskKey, In>()
  private val outputs = ConcurrentHashMap<TaskKey, Output<*>>()
  private val observables = ConcurrentHashMap<TaskKey,Observability>()
  private val taskReqs = ConcurrentHashMap<TaskKey, ArrayList<TaskRequireDep>>()
  private val callersOf = ConcurrentHashMap<TaskKey, MutableSet<TaskKey>>()
  private val fileReqs = ConcurrentHashMap<TaskKey, ArrayList<ResourceRequireDep>>()
  private val requireesOf = ConcurrentHashMap<ResourceKey, MutableSet<TaskKey>>()
  private val fileGens = ConcurrentHashMap<TaskKey, ArrayList<ResourceProvideDep>>()
  private val generatorOf = ConcurrentHashMap<ResourceKey, TaskKey?>()


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

  override fun observability(key: TaskKey) : Observability = observables.getOrDefault(key,Observability.Detached)

  override fun setObservability(key: TaskKey, observability: Observability) = observables.set(key ,observability)

  override fun taskRequires(key: TaskKey) = taskReqs.getOrEmptyList(key)
  override fun callersOf(key: TaskKey): Set<TaskKey> = callersOf.getOrPutSet(key)
  override fun setTaskRequires(key: TaskKey, taskRequires: ArrayList<TaskRequireDep>) {
    // Remove old call requirements
    val oldTaskReqs = this.taskReqs.remove(key)
    if(oldTaskReqs != null) {
      for(taskReq in oldTaskReqs) {
        callersOf.getOrPutSet(taskReq.callee).remove(key)
      }
    }
    // OPTO: diff taskReqs and oldCallReqs, remove/add entries based on diff.
    // Add new call requirements
    this.taskReqs[key] = taskRequires
    for(taskReq in taskRequires) {
      callersOf.getOrPutSet(taskReq.callee).add(key)
    }
  }

  override fun resourceRequires(key: TaskKey) = fileReqs.getOrEmptyList(key)
  override fun requireesOf(key: ResourceKey): Set<TaskKey> = requireesOf.getOrPutSet(key)
  override fun setResourceRequires(key: TaskKey, resourceRequires: ArrayList<ResourceRequireDep>) {
    // Remove old file requirements
    val oldFileReqs = this.fileReqs.remove(key)
    if(oldFileReqs != null) {
      for(fileReq in oldFileReqs) {
        requireesOf.getOrPutSet(fileReq.key).remove(key)
      }
    }
    // OPTO: diff fileReqs and oldPathReqs, remove/add entries based on diff.
    // Add new call requirements
    this.fileReqs[key] = resourceRequires
    for(fileReq in resourceRequires) {
      requireesOf.getOrPutSet(fileReq.key).add(key)
    }
  }

  override fun resourceProvides(key: TaskKey) = fileGens.getOrEmptyList(key)
  override fun providerOf(key: ResourceKey): TaskKey? = generatorOf[key]
  override fun setResourceProvides(key: TaskKey, resourceProvides: ArrayList<ResourceProvideDep>) {
    // Remove old file generators
    val oldFileGens = this.fileGens.remove(key)
    if(oldFileGens != null) {
      for(fileGen in oldFileGens) {
        generatorOf.remove(fileGen.key)
      }
    }
    // OPTO: diff fileGens and oldPathGens, remove/add entries based on diff.
    // Add new file generators
    this.fileGens[key] = resourceProvides
    for(fileGen in resourceProvides) {
      generatorOf[fileGen.key] = key
    }
  }

  override fun data(key: TaskKey): TaskData<*, *>? {
    val input = input(key) ?: return null
    val output = output(key) ?: return null
    val observability = observability(key)
    val callReqs = taskRequires(key)
    val pathReqs = resourceRequires(key)
    val pathGens = resourceProvides(key)
    return TaskData(input, output.output, callReqs, pathReqs, pathGens,observability)
  }

  override fun setData(key: TaskKey, data: TaskData<*, *>) {
    val (input, output, callReqs, pathReqs, pathGens,observability) = data
    setInput(key, input)
    setOutput(key, output)
    setObservability(key,observability)
    setTaskRequires(key, callReqs)
    setResourceRequires(key, pathReqs)
    setResourceProvides(key, pathGens)

  }


  override fun numSourceFiles(): Int {
    var numSourceFiles = 0
    for(file in requireesOf.keys) {
      if(!generatorOf.containsKey(file)) {
        ++numSourceFiles
      }
    }
    return numSourceFiles
  }


  override fun drop() {
    outputs.clear()
    taskReqs.clear()
    callersOf.clear()
    fileReqs.clear()
    requireesOf.clear()
    fileGens.clear()
    generatorOf.clear()
  }


  override fun toString(): String {
    return "InMemoryStore"
  }

  fun dump() : StoreDump {
    return StoreDump(HashMap(inputs),
            HashMap(outputs),
            HashMap(taskReqs),
            HashMap(callersOf),
            HashMap(fileReqs),
            HashMap(requireesOf),
            HashMap(fileGens),
            HashMap(generatorOf),
            HashMap(observables)
    )
  }
}

@Suppress("NOTHING_TO_INLINE")
private inline fun <K, V> ConcurrentHashMap<K, MutableSet<V>>.getOrPutSet(key: K) = this.getOrPut(key) { ConcurrentHashMap.newKeySet<V>() }!!

@Suppress("NOTHING_TO_INLINE")
private inline fun <K, V> ConcurrentHashMap<K, ArrayList<V>>.getOrEmptyList(key: K) = this.getOrElse(key) { arrayListOf() }




data class StoreDump(
        val inputs: HashMap<TaskKey,In>,
        val outputs : HashMap<TaskKey, Output<*>>,
        val taskReqs : HashMap<TaskKey, ArrayList<TaskRequireDep>>,
        val callersOf : HashMap<TaskKey, MutableSet<TaskKey>>,
        val fileReqs : HashMap<TaskKey, ArrayList<ResourceRequireDep>>,
        val requireesOf : HashMap<ResourceKey, MutableSet<TaskKey>>,
        val fileGens : HashMap<TaskKey, ArrayList<ResourceProvideDep>>,
        val generatorOf : HashMap<ResourceKey, TaskKey?>,
        val observables : HashMap<TaskKey,Observability>
)
