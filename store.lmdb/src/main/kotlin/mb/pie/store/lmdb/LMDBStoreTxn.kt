package mb.pie.store.lmdb

import mb.pie.api.*
import java.nio.ByteBuffer
import java.util.function.Function

public open class LMDBStoreTxn : StoreReadTxn,StoreWriteTxn {
  private val txn: TxnB;
  private val logger: Logger;
  private val inputDb: DbiB;
  private val outputDb: DbiB;
  private val taskRequiresDb: DbiB;
  private val callersOfDb: DbiB;
  private val callersOfValuesDb: DbiB;
  private val resourceRequiresDb: DbiB;
  private val requireesOfDb: DbiB;
  private val requireesOfValuesDb: DbiB;
  private val resourceProvidesDb: DbiB;
  private val providerOfDb: DbiB;
  private val shared: DbiShared;


  public constructor(
    env: EnvB,
    txn: TxnB,
    isWriteTxn: Boolean,
    logger: Logger,
    inputDb: DbiB,
    outputDb: DbiB,
    taskRequiresDb: DbiB,
    callersOfDb: DbiB,
    callersOfValuesDb: DbiB,
    resourceRequiresDb: DbiB,
    requireesOfDb: DbiB,
    requireesOfValuesDb: DbiB,
    resourceProvidesDb: DbiB,
    providerOfDb: DbiB
  ) {
    this.txn = txn;
    this.logger = logger;
    this.inputDb = inputDb;
    this.outputDb = outputDb;
    this.taskRequiresDb = taskRequiresDb;
    this.callersOfDb = callersOfDb;
    this.callersOfValuesDb = callersOfValuesDb;
    this.resourceRequiresDb = resourceRequiresDb;
    this.requireesOfDb = requireesOfDb;
    this.requireesOfValuesDb = requireesOfValuesDb;
    this.resourceProvidesDb = resourceProvidesDb;
    this.providerOfDb = providerOfDb;
    this.shared = DbiShared(env,txn,isWriteTxn,logger);
  }

  override fun close() {
    txn.commit();
    txn.close();
  }


  override fun input(key: TaskKey): In? {
    return Deserialized.orElse(shared.getOne<In>(BufferUtil.toBuffer(SerializeUtil.hash(SerializeUtil.serialize(key))),inputDb),null)
  }

  override fun output(key: TaskKey): Output<*>? {
    val keyHashedBuf: ByteBuffer = BufferUtil.toBuffer(SerializeUtil.hash(SerializeUtil.serialize(key)));
    val deserialized: Deserialized<Out>? = shared.getOne<Out>(keyHashedBuf,outputDb);
    return Deserialized.mapOrElse<Out,Output<*>?>(deserialized,null,Function { value -> Output(value) });
  }

  override fun taskRequires(key: TaskKey): ArrayList<TaskRequireDep> {
    return Deserialized.orElse(shared.getOne<ArrayList<TaskRequireDep>>(BufferUtil.toBuffer(SerializeUtil.hash(SerializeUtil.serialize(key))),taskRequiresDb),arrayListOf());
  }

  override fun callersOf(key: TaskKey): Set<TaskKey> {
    return shared.getMultiple(BufferUtil.toBuffer(SerializeUtil.hash(SerializeUtil.serialize(key))),callersOfDb,callersOfValuesDb);
  }

  override fun resourceRequires(key: TaskKey): ArrayList<ResourceRequireDep> {
    return Deserialized.orElse(shared.getOne<ArrayList<ResourceRequireDep>>(BufferUtil.toBuffer(SerializeUtil.hash(SerializeUtil.serialize(key))),resourceRequiresDb),arrayListOf());
  }

  override fun requireesOf(key: ResourceKey): Set<TaskKey> {
    return shared.getMultiple(BufferUtil.toBuffer(SerializeUtil.hash(SerializeUtil.serialize(key))),requireesOfDb,requireesOfValuesDb);
  }

  override fun resourceProvides(key: TaskKey): ArrayList<ResourceProvideDep> {
    return Deserialized.orElse(shared.getOne<ArrayList<ResourceProvideDep>>(BufferUtil.toBuffer(SerializeUtil.hash(SerializeUtil.serialize(key))),resourceProvidesDb),arrayListOf());
  }

  override fun providerOf(key: ResourceKey): TaskKey? {
    return Deserialized.orElse(shared.getOne<TaskKey?>(BufferUtil.toBuffer(SerializeUtil.hash(SerializeUtil.serialize(key))),providerOfDb),null);
  }

  override fun data(key: TaskKey): TaskData<*,*>? {
    // OPTO: reuse buffers? is that safe?
    val keyHashedBytes: ByteArray = SerializeUtil.hash(SerializeUtil.serialize(key));
    val inputDeserialized: Deserialized<In>? = shared.getOne<In>(BufferUtil.toBuffer(keyHashedBytes),inputDb);
    if(inputDeserialized == null || inputDeserialized.failed) {
      return null;
    }
    val outputDeserialized: Deserialized<Out>? = shared.getOne<Out>(BufferUtil.toBuffer(keyHashedBytes),outputDb);
    if(outputDeserialized == null || outputDeserialized.failed) {
      return null;
    }
    val input: In = inputDeserialized.deserialized;
    val output: Out = outputDeserialized.deserialized;
    val taskRequires: ArrayList<TaskRequireDep> = Deserialized.orElse(shared.getOne<ArrayList<TaskRequireDep>>(BufferUtil.toBuffer(keyHashedBytes),taskRequiresDb),arrayListOf());
    val resourceRequires: ArrayList<ResourceRequireDep> = Deserialized.orElse(shared.getOne<ArrayList<ResourceRequireDep>>(BufferUtil.toBuffer(keyHashedBytes),resourceRequiresDb),arrayListOf());
    val resourceProvides: ArrayList<ResourceProvideDep> = Deserialized.orElse(shared.getOne<ArrayList<ResourceProvideDep>>(BufferUtil.toBuffer(keyHashedBytes),resourceProvidesDb),arrayListOf());
    return TaskData(input,output,taskRequires,resourceRequires,resourceProvides);
  }

  override fun numSourceFiles(): Int {
    // Cannot use files in requireesOfValuesDb, as these are never cleaned up at the moment. Instead use values of resourceRequiresDb.
    val requiredFiles: HashSet<ResourceKey> = hashSetOf<ResourceKey>();
    resourceRequiresDb.iterate(txn).use { cursor ->
      cursor.iterable().flatMapTo(requiredFiles) { keyVal ->
        Deserialized.orElse(SerializeUtil.deserialize<ArrayList<ResourceRequireDep>>(keyVal.`val`(),logger),arrayListOf()).map { it.key }
      }
    }

    var numSourceFiles: Int = 0;
    for(file: ResourceKey in requiredFiles) {
      if(!shared.getBool(BufferUtil.toBuffer(SerializeUtil.hash(SerializeUtil.serialize(file))),providerOfDb)) {
        ++numSourceFiles;
      }
    }
    return numSourceFiles;
  }


  override fun setInput(key: TaskKey,input: In) {
    shared.setOne(BufferUtil.toBuffer(SerializeUtil.hash(SerializeUtil.serialize(key))),BufferUtil.toBuffer(SerializeUtil.serialize(input)),inputDb);
  }

  override fun setOutput(key: TaskKey,output: Out) {
    shared.setOne(BufferUtil.toBuffer(SerializeUtil.hash(SerializeUtil.serialize(key))),BufferUtil.toBuffer(SerializeUtil.serialize(output)),outputDb);
  }

  override fun setTaskRequires(key: TaskKey,taskRequires: ArrayList<TaskRequireDep>) {
    // OPTO: reuse buffers? is that safe?
    val serializedAndHashed: SerializedAndHashed = SerializeUtil.serializeAndHash(key)
    val keyBytes: ByteArray = serializedAndHashed.serialized;
    val keyHashedBytes: ByteArray = serializedAndHashed.hashed;

    // Remove old inverse task requirements.
    val oldTaskRequires: ArrayList<TaskRequireDep> = Deserialized.orElse(shared.getOne<ArrayList<TaskRequireDep>>(BufferUtil.toBuffer(keyHashedBytes),taskRequiresDb),arrayListOf());
    for(taskRequire: TaskRequireDep in oldTaskRequires) {
      shared.deleteDup(BufferUtil.toBuffer(SerializeUtil.hash(SerializeUtil.serialize(taskRequire.callee))),BufferUtil.toBuffer(keyHashedBytes),callersOfDb,callersOfValuesDb);
    }

    // Add new task requirements.
    shared.setOne(BufferUtil.toBuffer(keyHashedBytes),BufferUtil.toBuffer(SerializeUtil.serialize(taskRequires)),taskRequiresDb);
    for(taskRequire: TaskRequireDep in taskRequires) {
      shared.setDup(BufferUtil.toBuffer(SerializeUtil.hash(SerializeUtil.serialize(taskRequire.callee))),BufferUtil.toBuffer(keyBytes),BufferUtil.toBuffer(keyHashedBytes),callersOfDb,callersOfValuesDb);
    }
  }

  override fun setResourceRequires(key: TaskKey,resourceRequires: ArrayList<ResourceRequireDep>) {
    // OPTO: reuse buffers? is that safe?
    val serializedAndHashed: SerializedAndHashed = SerializeUtil.serializeAndHash(key)
    val keyBytes: ByteArray = serializedAndHashed.serialized;
    val keyHashedBytes: ByteArray = serializedAndHashed.hashed;

    // Remove old inverse file requirements.
    val oldResourceRequires = Deserialized.orElse(shared.getOne<ArrayList<ResourceRequireDep>>(BufferUtil.toBuffer(keyHashedBytes),resourceRequiresDb),arrayListOf());
    for(resourceRequire in oldResourceRequires) {
      shared.deleteDup(BufferUtil.toBuffer(SerializeUtil.hash(SerializeUtil.serialize(resourceRequire.key))),BufferUtil.toBuffer(keyHashedBytes),requireesOfDb,requireesOfValuesDb);
    }

    // Add new file requirements.
    shared.setOne(BufferUtil.toBuffer(keyHashedBytes),BufferUtil.toBuffer(SerializeUtil.serialize(resourceRequires)),resourceRequiresDb);
    for(resourceRequire in resourceRequires) {
      shared.setDup(BufferUtil.toBuffer(SerializeUtil.hash(SerializeUtil.serialize(resourceRequire.key))),BufferUtil.toBuffer(keyBytes),BufferUtil.toBuffer(keyHashedBytes),requireesOfDb,requireesOfValuesDb);
    }
  }

  override fun setResourceProvides(key: TaskKey,resourceProvides: ArrayList<ResourceProvideDep>) {
    // OPTO: reuse buffers? is that safe?
    val serializedAndHashed: SerializedAndHashed = SerializeUtil.serializeAndHash(key)
    val keyBytes: ByteArray = serializedAndHashed.serialized;
    val keyHashedBytes: ByteArray = serializedAndHashed.hashed;

    // Remove old inverse file generates.
    val oldResourceProvides: ArrayList<ResourceProvideDep> = Deserialized.orElse(shared.getOne<ArrayList<ResourceProvideDep>>(BufferUtil.toBuffer(keyHashedBytes),resourceProvidesDb),arrayListOf());
    for(resourceProvide: ResourceProvideDep in oldResourceProvides) {
      shared.deleteOne(BufferUtil.toBuffer(SerializeUtil.hash(SerializeUtil.serialize(resourceProvide.key))),providerOfDb);
    }

    // Add new file generates.
    shared.setOne(BufferUtil.toBuffer(keyHashedBytes),BufferUtil.toBuffer(SerializeUtil.serialize(resourceProvides)),resourceProvidesDb);
    for(resourceProvide: ResourceProvideDep in resourceProvides) {
      shared.setOne(BufferUtil.toBuffer(SerializeUtil.hash(SerializeUtil.serialize(resourceProvide.key))),BufferUtil.toBuffer(keyBytes),providerOfDb);
    }
  }

  override fun setData(key: TaskKey,data: TaskData<*,*>) {
    // OPTO: serialize and hash task only once
    setInput(key,data.input);
    setOutput(key,data.output);
    setTaskRequires(key,data.taskRequires);
    setResourceRequires(key,data.resourceRequires);
    setResourceProvides(key,data.resourceProvides);
  }

  override fun drop() {
    inputDb.drop(txn);
    outputDb.drop(txn);
    taskRequiresDb.drop(txn);
    callersOfDb.drop(txn);
    callersOfValuesDb.drop(txn);
    resourceRequiresDb.drop(txn);
    requireesOfDb.drop(txn);
    requireesOfValuesDb.drop(txn);
    resourceProvidesDb.drop(txn);
    providerOfDb.drop(txn);
  }
}