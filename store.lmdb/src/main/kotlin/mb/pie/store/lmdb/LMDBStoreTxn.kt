package mb.pie.store.lmdb

import mb.pie.api.*
import mb.pie.vfs.path.PPath

internal open class LMDBStoreTxn(
  env: EnvB,
  private val txn: TxnB,
  isWriteTxn: Boolean,
  private val logger: Logger,
  private val inputDb: DbiB,
  private val outputDb: DbiB,
  private val taskReqsDb: DbiB,
  private val callersOfDb: DbiB,
  private val callersOfValuesDb: DbiB,
  private val fileReqsDb: DbiB,
  private val requireesOfDb: DbiB,
  private val requireesOfValuesDb: DbiB,
  private val fileGensDb: DbiB,
  private val generatorOfDb: DbiB
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

  override fun taskReqs(key: TaskKey): ArrayList<TaskReq> {
    return shared.getOne<ArrayList<TaskReq>>(key.serialize().hash().toBuffer(), taskReqsDb).orElse(arrayListOf())
  }

  override fun callersOf(key: TaskKey): Set<TaskKey> {
    return shared.getMultiple(key.serialize().hash().toBuffer(), callersOfDb, callersOfValuesDb)
  }

  override fun fileReqs(key: TaskKey): ArrayList<ResourceRequire> {
    return shared.getOne<ArrayList<ResourceRequire>>(key.serialize().hash().toBuffer(), fileReqsDb).orElse(arrayListOf())
  }

  override fun requireesOf(file: ResourceKey): Set<TaskKey> {
    return shared.getMultiple(file.serialize().hash().toBuffer(), requireesOfDb, requireesOfValuesDb)
  }

  override fun fileGens(key: TaskKey): ArrayList<ResourceProvide> {
    return shared.getOne<ArrayList<ResourceProvide>>(key.serialize().hash().toBuffer(), fileGensDb).orElse(arrayListOf())
  }

  override fun generatorOf(file: ResourceKey): TaskKey? {
    return shared.getOne<TaskKey?>(file.serialize().hash().toBuffer(), generatorOfDb).orElse(null)
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
    val taskReqs = shared.getOne<ArrayList<TaskReq>>(keyHashedBytes.toBuffer(), taskReqsDb).orElse(arrayListOf())
    val fileReqs = shared.getOne<ArrayList<ResourceRequire>>(keyHashedBytes.toBuffer(), fileReqsDb).orElse(arrayListOf())
    val fileGens = shared.getOne<ArrayList<ResourceProvide>>(keyHashedBytes.toBuffer(), fileGensDb).orElse(arrayListOf())
    return TaskData(input, output, taskReqs, fileReqs, fileGens)
  }

  override fun numSourceFiles(): Int {
    // Cannot use files in requireesOfValuesDb, as these are never cleaned up at the moment. Instead use values of fileReqsDb.
    val requiredFiles = run {
      val set = hashSetOf<ResourceKey>()
      fileReqsDb.iterate(txn).use { cursor ->
        cursor.iterable().flatMapTo(set) { keyVal ->
          keyVal.`val`().deserialize<ArrayList<ResourceRequire>>(logger).orElse(arrayListOf()).map { it.key }
        }
      }
      set
    }
    var numSourceFiles = 0
    for(file in requiredFiles) {
      if(!shared.getBool(file.serialize().hash().toBuffer(), generatorOfDb)) {
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

  override fun setTaskReqs(key: TaskKey, taskReqs: ArrayList<TaskReq>) {
    // OPTO: reuse buffers? is that safe?
    val (keyBytes, keyHashedBytes) = key.serializeAndHash()

    // Remove old inverse task requirements.
    val oldTaskReqs = shared.getOne<ArrayList<TaskReq>>(keyHashedBytes.toBuffer(), taskReqsDb).orElse(arrayListOf())
    for(taskReq in oldTaskReqs) {
      shared.deleteDup(taskReq.callee.serialize().hash().toBuffer(), keyHashedBytes.toBuffer(), callersOfDb, callersOfValuesDb)
    }

    // Add new task requirements.
    shared.setOne(keyHashedBytes.toBuffer(), taskReqs.serialize().toBuffer(), taskReqsDb)
    for(taskReq in taskReqs) {
      shared.setDup(taskReq.callee.serialize().hash().toBuffer(), keyBytes.toBuffer(), keyHashedBytes.toBuffer(), callersOfDb, callersOfValuesDb)
    }
  }

  override fun setFileReqs(key: TaskKey, fileReqs: ArrayList<ResourceRequire>) {
    // OPTO: reuse buffers? is that safe?
    val (keyBytes, keyHashedBytes) = key.serializeAndHash()

    // Remove old inverse file requirements.
    val oldFileReqs = shared.getOne<ArrayList<ResourceRequire>>(keyHashedBytes.toBuffer(), fileReqsDb).orElse(arrayListOf())
    for(fileReq in oldFileReqs) {
      shared.deleteDup(fileReq.key.serialize().hash().toBuffer(), keyHashedBytes.toBuffer(), requireesOfDb, requireesOfValuesDb)
    }

    // Add new file requirements.
    shared.setOne(keyHashedBytes.toBuffer(), fileReqs.serialize().toBuffer(), fileReqsDb)
    for(fileReq in fileReqs) {
      shared.setDup(fileReq.key.serialize().hash().toBuffer(), keyBytes.toBuffer(), keyHashedBytes.toBuffer(), requireesOfDb, requireesOfValuesDb)
    }
  }

  override fun setFileGens(key: TaskKey, fileGens: ArrayList<ResourceProvide>) {
    // OPTO: reuse buffers? is that safe?
    val (keyBytes, keyHashedBytes) = key.serializeAndHash()

    // Remove old inverse file generates.
    val oldFileGens = shared.getOne<ArrayList<ResourceProvide>>(keyHashedBytes.toBuffer(), fileGensDb).orElse(arrayListOf())
    for(fileGen in oldFileGens) {
      shared.deleteOne(fileGen.key.serialize().hash().toBuffer(), generatorOfDb)
    }

    // Add new file generates.
    shared.setOne(keyHashedBytes.toBuffer(), fileGens.serialize().toBuffer(), fileGensDb)
    for(fileGen in fileGens) {
      shared.setOne(fileGen.key.serialize().hash().toBuffer(), keyBytes.toBuffer(), generatorOfDb)
    }
  }

  override fun setData(key: TaskKey, data: TaskData<*, *>) {
    // OPTO: serialize and hash task only once
    setInput(key, data.input)
    setOutput(key, data.output)
    setTaskReqs(key, data.taskReqs)
    setFileReqs(key, data.fileReqs)
    setFileGens(key, data.fileGens)
  }

  override fun drop() {
    inputDb.drop(txn)
    outputDb.drop(txn)
    taskReqsDb.drop(txn)
    callersOfDb.drop(txn)
    callersOfValuesDb.drop(txn)
    fileReqsDb.drop(txn)
    requireesOfDb.drop(txn)
    requireesOfValuesDb.drop(txn)
    fileGensDb.drop(txn)
    generatorOfDb.drop(txn)
  }


  override fun close() {
    txn.commit()
    txn.close()
  }
}