package mb.pie.runtime.store

import mb.pie.api.*
import java.io.Serializable
import java.util.concurrent.ConcurrentHashMap

public class InMemoryStore : Store, StoreReadTxn, StoreWriteTxn {
  private val inputs: ConcurrentHashMap<TaskKey, In> = ConcurrentHashMap<TaskKey, In>()
  private val outputs: ConcurrentHashMap<TaskKey, Output<*>> = ConcurrentHashMap<TaskKey, Output<*>>()
  private val taskReqs: ConcurrentHashMap<TaskKey, ArrayList<TaskRequireDep>> = ConcurrentHashMap<TaskKey, ArrayList<TaskRequireDep>>()
  private val callersOf: ConcurrentHashMap<TaskKey, MutableSet<TaskKey>> = ConcurrentHashMap<TaskKey, MutableSet<TaskKey>>()
  private val fileReqs: ConcurrentHashMap<TaskKey, ArrayList<ResourceRequireDep>> = ConcurrentHashMap<TaskKey, ArrayList<ResourceRequireDep>>()
  private val requireesOf: ConcurrentHashMap<ResourceKey, MutableSet<TaskKey>> = ConcurrentHashMap<ResourceKey, MutableSet<TaskKey>>()
  private val fileGens: ConcurrentHashMap<TaskKey, ArrayList<ResourceProvideDep>> = ConcurrentHashMap<TaskKey, ArrayList<ResourceProvideDep>>()
  private val generatorOf: ConcurrentHashMap<ResourceKey, TaskKey?> = ConcurrentHashMap<ResourceKey, TaskKey?>()


  companion object {
    private fun <K, V> getOrPutSet(map: ConcurrentHashMap<K, MutableSet<V>>, key: K): MutableSet<V> {
      return map.getOrPut(key) { ConcurrentHashMap.newKeySet<V>() }!!;
    }

    private fun <K, V> getOrEmptyList(map: ConcurrentHashMap<K, ArrayList<V>>, key: K): ArrayList<V> {
      return map.getOrElse(key) { arrayListOf() };
    }
  }


  override fun readTxn(): InMemoryStore {
    return this;
  }

  override fun writeTxn(): InMemoryStore {
    return this;
  }

  override fun sync() {

  }

  override fun close() {

  }


  override fun input(key: TaskKey): In? {
    return inputs[key];
  }

  override fun setInput(key: TaskKey, input: In) {
    inputs[key] = input;
  }

  override fun output(key: TaskKey): Output<Serializable>? {
    if(!outputs.containsKey(key)) {
      return null;
    } else {
      val wrapper: Output<*> = outputs[key]!!;
      return Output(wrapper.output);
    }
  }

  override fun setOutput(key: TaskKey, output: Out) {
    // ConcurrentHashMap does not support null values, so also wrap outputs (which can be null) in an Output object.
    outputs[key] = Output(output);
  }

  override fun taskRequires(key: TaskKey): ArrayList<TaskRequireDep> {
    return getOrEmptyList(taskReqs, key);
  }

  override fun callersOf(key: TaskKey): Set<TaskKey> {
    return getOrPutSet(callersOf, key);
  }

  override fun setTaskRequires(key: TaskKey, taskRequires: ArrayList<TaskRequireDep>) {
    // Remove old call requirements
    val oldTaskReqs: ArrayList<TaskRequireDep>? = this.taskReqs.remove(key);
    if(oldTaskReqs != null) {
      for(taskReq: TaskRequireDep in oldTaskReqs) {
        getOrPutSet(callersOf, taskReq.callee).remove(key);
      }
    }
    // OPTO: diff taskReqs and oldCallReqs, remove/add entries based on diff.
    // Add new call requirements
    this.taskReqs[key] = taskRequires;
    for(taskReq: TaskRequireDep in taskRequires) {
      getOrPutSet(callersOf, taskReq.callee).add(key);
    }
  }

  override fun resourceRequires(key: TaskKey): ArrayList<ResourceRequireDep> {
    return getOrEmptyList(fileReqs, key);
  }

  override fun requireesOf(key: ResourceKey): Set<TaskKey> {
    return getOrPutSet(requireesOf, key);
  }

  override fun setResourceRequires(key: TaskKey, resourceRequires: ArrayList<ResourceRequireDep>) {
    // Remove old file requirements
    val oldFileReqs: ArrayList<ResourceRequireDep>? = this.fileReqs.remove(key);
    if(oldFileReqs != null) {
      for(fileReq: ResourceRequireDep in oldFileReqs) {
        getOrPutSet(requireesOf, fileReq.key).remove(key);
      }
    }
    // OPTO: diff fileReqs and oldPathReqs, remove/add entries based on diff.
    // Add new call requirements
    this.fileReqs[key] = resourceRequires;
    for(fileReq: ResourceRequireDep in resourceRequires) {
      getOrPutSet(requireesOf, fileReq.key).add(key);
    }
  }

  override fun resourceProvides(key: TaskKey): ArrayList<ResourceProvideDep> {
    return getOrEmptyList(fileGens, key);
  }

  override fun providerOf(key: ResourceKey): TaskKey? {
    return generatorOf[key];
  }

  override fun setResourceProvides(key: TaskKey, resourceProvides: ArrayList<ResourceProvideDep>) {
    // Remove old file generators
    val oldFileGens: ArrayList<ResourceProvideDep>? = this.fileGens.remove(key);
    if(oldFileGens != null) {
      for(fileGen: ResourceProvideDep in oldFileGens) {
        generatorOf.remove(fileGen.key);
      }
    }
    // OPTO: diff fileGens and oldPathGens, remove/add entries based on diff.
    // Add new file generators
    this.fileGens[key] = resourceProvides;
    for(fileGen: ResourceProvideDep in resourceProvides) {
      generatorOf[fileGen.key] = key;
    }
  }

  override fun data(key: TaskKey): TaskData<*, *>? {
    val input: In? = input(key);
    if(input == null) {
      return null;
    }
    val output: Output<Serializable>? = output(key);
    if(output == null) {
      return null;
    }
    val callReqs: ArrayList<TaskRequireDep> = taskRequires(key);
    val pathReqs: ArrayList<ResourceRequireDep> = resourceRequires(key);
    val pathGens: ArrayList<ResourceProvideDep> = resourceProvides(key);
    return TaskData(input, output.output, callReqs, pathReqs, pathGens);
  }

  override fun setData(key: TaskKey, data: TaskData<*, *>) {
    setInput(key, data.input);
    setOutput(key, data.output);
    setTaskRequires(key, data.taskRequires);
    setResourceRequires(key, data.resourceRequires);
    setResourceProvides(key, data.resourceProvides);
  }


  override fun numSourceFiles(): Int {
    var numSourceFiles: Int = 0;
    for(file: ResourceKey in requireesOf.keys) {
      if(!generatorOf.containsKey(file)) {
        ++numSourceFiles;
      }
    }
    return numSourceFiles;
  }


  override fun drop() {
    outputs.clear();
    taskReqs.clear();
    callersOf.clear();
    fileReqs.clear();
    requireesOf.clear();
    fileGens.clear();
    generatorOf.clear();
  }


  override fun toString(): String {
    return "InMemoryStore()";
  }
}
