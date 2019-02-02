package mb.pie.store.lmdb

import mb.pie.api.*

internal open class LMDBStoreTxn(
  env: EnvB,
  private val txn: TxnB,
  isWriteTxn: Boolean,
  private val logger: Logger,
  private val inputDb: DbiB,
  private val outputDb: DbiB,
  private val taskRequiresDb: DbiB,
  private val callersOfDb: DbiB,
  private val callersOfValuesDb: DbiB,
  private val resourceRequiresDb: DbiB,
  private val requireesOfDb: DbiB,
  private val requireesOfValuesDb: DbiB,
  private val resourceProvidesDb: DbiB,
  private val providerOfDb: DbiB
) : StoreReadTxn, StoreWriteTxn {
  private val shared = DbiShared(env, txn, isWriteTxn, logger)


  override fun input(key: TaskKey): In? {
    return shared.getOne<In>(key.serialize().hash().toBuffer(), inputDb).orElse(null)
  }

  override fun output(key: TaskKey): Output<*>? {
    return shared.getOne<Out>(key.serialize().hash().toBuffer(), outputDb).mapOrElse(null) { value ->
      Output(value)
    }
  }

  override fun observability(key: TaskKey) : Observability {
    println("LMDB Does not yet support observability");
    return Observability.Attached
  }

  override fun setObservability(key: TaskKey, observability: Observability) {
    println("LMDB Does not yet support observability");
  }



  override fun taskRequires(key: TaskKey): ArrayList<TaskRequireDep> {
    return shared.getOne<ArrayList<TaskRequireDep>>(key.serialize().hash().toBuffer(), taskRequiresDb).orElse(arrayListOf())
  }

  override fun callersOf(key: TaskKey): Set<TaskKey> {
    return shared.getMultiple(key.serialize().hash().toBuffer(), callersOfDb, callersOfValuesDb)
  }

  override fun resourceRequires(key: TaskKey): ArrayList<ResourceRequireDep> {
    return shared.getOne<ArrayList<ResourceRequireDep>>(key.serialize().hash().toBuffer(), resourceRequiresDb).orElse(arrayListOf())
  }

  override fun requireesOf(key: ResourceKey): Set<TaskKey> {
    return shared.getMultiple(key.serialize().hash().toBuffer(), requireesOfDb, requireesOfValuesDb)
  }

  override fun resourceProvides(key: TaskKey): ArrayList<ResourceProvideDep> {
    return shared.getOne<ArrayList<ResourceProvideDep>>(key.serialize().hash().toBuffer(), resourceProvidesDb).orElse(arrayListOf())
  }

  override fun providerOf(key: ResourceKey): TaskKey? {
    return shared.getOne<TaskKey?>(key.serialize().hash().toBuffer(), providerOfDb).orElse(null)
  }

  override fun data(key: TaskKey): TaskData<*, *>? {
    // OPTO: reuse buffers? is that safe?
    val keyHashedBytes = key.serialize().hash()
    val inputDeserialized = shared.getOne<In>(keyHashedBytes.toBuffer(), inputDb)
    if(inputDeserialized == null || inputDeserialized.failed) {
      return null
    }
    val outputDeserialized = shared.getOne<Out>(keyHashedBytes.toBuffer(), outputDb)
    if(outputDeserialized == null || outputDeserialized.failed) {
      return null
    }
    val input = inputDeserialized.deserialized
    val output = outputDeserialized.deserialized
    val observability = Observability.Attached; // TODO;
    println("LMDB Observability does not work yet");
    val taskRequires = shared.getOne<ArrayList<TaskRequireDep>>(keyHashedBytes.toBuffer(), taskRequiresDb).orElse(arrayListOf())
    val resourceRequires = shared.getOne<ArrayList<ResourceRequireDep>>(keyHashedBytes.toBuffer(), resourceRequiresDb).orElse(arrayListOf())
    val resourceProvides = shared.getOne<ArrayList<ResourceProvideDep>>(keyHashedBytes.toBuffer(), resourceProvidesDb).orElse(arrayListOf())
    return TaskData(input, output, taskRequires, resourceRequires, resourceProvides, observability )
  }

  override fun numSourceFiles(): Int {
    // Cannot use files in requireesOfValuesDb, as these are never cleaned up at the moment. Instead use values of resourceRequiresDb.
    val requiredFiles = run {
      val set = hashSetOf<ResourceKey>()
      resourceRequiresDb.iterate(txn).use { cursor ->
        cursor.iterable().flatMapTo(set) { keyVal ->
          keyVal.`val`().deserialize<ArrayList<ResourceRequireDep>>(logger).orElse(arrayListOf()).map { it.key }
        }
      }
      set
    }
    var numSourceFiles = 0
    for(file in requiredFiles) {
      if(!shared.getBool(file.serialize().hash().toBuffer(), providerOfDb)) {
        ++numSourceFiles
      }
    }
    return numSourceFiles
  }


  override fun setInput(key: TaskKey, input: In) {
    shared.setOne(key.serialize().hash().toBuffer(), input.serialize().toBuffer(), inputDb)
  }

  override fun setOutput(key: TaskKey, output: Out) {
    shared.setOne(key.serialize().hash().toBuffer(), output.serialize().toBuffer(), outputDb)
  }

  override fun setTaskRequires(key: TaskKey, taskRequires: ArrayList<TaskRequireDep>) {
    // OPTO: reuse buffers? is that safe?
    val (keyBytes, keyHashedBytes) = key.serializeAndHash()

    // Remove old inverse task requirements.
    val oldTaskRequires = shared.getOne<ArrayList<TaskRequireDep>>(keyHashedBytes.toBuffer(), taskRequiresDb).orElse(arrayListOf())
    for(taskRequire in oldTaskRequires) {
      shared.deleteDup(taskRequire.callee.serialize().hash().toBuffer(), keyHashedBytes.toBuffer(), callersOfDb, callersOfValuesDb)
    }

    // Add new task requirements.
    shared.setOne(keyHashedBytes.toBuffer(), taskRequires.serialize().toBuffer(), taskRequiresDb)
    for(taskRequire in taskRequires) {
      shared.setDup(taskRequire.callee.serialize().hash().toBuffer(), keyBytes.toBuffer(), keyHashedBytes.toBuffer(), callersOfDb, callersOfValuesDb)
    }
  }

  override fun setResourceRequires(key: TaskKey, resourceRequires: ArrayList<ResourceRequireDep>) {
    // OPTO: reuse buffers? is that safe?
    val (keyBytes, keyHashedBytes) = key.serializeAndHash()

    // Remove old inverse file requirements.
    val oldResourceRequires = shared.getOne<ArrayList<ResourceRequireDep>>(keyHashedBytes.toBuffer(), resourceRequiresDb).orElse(arrayListOf())
    for(resourceRequire in oldResourceRequires) {
      shared.deleteDup(resourceRequire.key.serialize().hash().toBuffer(), keyHashedBytes.toBuffer(), requireesOfDb, requireesOfValuesDb)
    }

    // Add new file requirements.
    shared.setOne(keyHashedBytes.toBuffer(), resourceRequires.serialize().toBuffer(), resourceRequiresDb)
    for(resourceRequire in resourceRequires) {
      shared.setDup(resourceRequire.key.serialize().hash().toBuffer(), keyBytes.toBuffer(), keyHashedBytes.toBuffer(), requireesOfDb, requireesOfValuesDb)
    }
  }

  override fun setResourceProvides(key: TaskKey, resourceProvides: ArrayList<ResourceProvideDep>) {
    // OPTO: reuse buffers? is that safe?
    val (keyBytes, keyHashedBytes) = key.serializeAndHash()

    // Remove old inverse file generates.
    val oldResourceProvides = shared.getOne<ArrayList<ResourceProvideDep>>(keyHashedBytes.toBuffer(), resourceProvidesDb).orElse(arrayListOf())
    for(resourceProvide in oldResourceProvides) {
      shared.deleteOne(resourceProvide.key.serialize().hash().toBuffer(), providerOfDb)
    }

    // Add new file generates.
    shared.setOne(keyHashedBytes.toBuffer(), resourceProvides.serialize().toBuffer(), resourceProvidesDb)
    for(resourceProvide in resourceProvides) {
      shared.setOne(resourceProvide.key.serialize().hash().toBuffer(), keyBytes.toBuffer(), providerOfDb)
    }
  }

  override fun setData(key: TaskKey, data: TaskData<*, *>) {
    // OPTO: serialize and hash task only once
    setInput(key, data.input)
    setOutput(key, data.output)
    setObservability(key, data.observability)
    setTaskRequires(key, data.taskRequires)
    setResourceRequires(key, data.resourceRequires)
    setResourceProvides(key, data.resourceProvides)
  }

  override fun drop() {
    inputDb.drop(txn)
    outputDb.drop(txn)
    taskRequiresDb.drop(txn)
    callersOfDb.drop(txn)
    callersOfValuesDb.drop(txn)
    resourceRequiresDb.drop(txn)
    requireesOfDb.drop(txn)
    requireesOfValuesDb.drop(txn)
    resourceProvidesDb.drop(txn)
    providerOfDb.drop(txn)
  }


  override fun close() {
    txn.commit()
    txn.close()
  }
}