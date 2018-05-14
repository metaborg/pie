package mb.pie.runtime.core.impl.store

import com.google.inject.Inject
import mb.log.Logger
import mb.pie.runtime.core.*
import mb.pie.runtime.core.impl.*
import mb.vfs.path.PPath
import org.agrona.DirectBuffer
import org.agrona.ExpandableDirectByteBuffer
import org.agrona.concurrent.UnsafeBuffer
import org.agrona.io.DirectBufferInputStream
import org.agrona.io.ExpandableDirectBufferOutputStream
import org.lmdbjava.*
import java.io.*
import java.nio.ByteBuffer
import java.security.DigestOutputStream
import java.security.MessageDigest

typealias Buf = DirectBuffer
typealias EnvB = Env<Buf>
typealias DbiB = Dbi<Buf>
typealias TxnB = Txn<Buf>

class LMDBBuildStoreFactory @Inject constructor(val logger: Logger) {
  fun create(envDir: File, maxDbSize: Int = 1024 * 1024 * 1024, maxReaders: Int = 1024): LMDBStore {
    return LMDBStore(logger.forContext(LMDBStore::class.java), envDir, maxDbSize, maxReaders)
  }
}

class LMDBStore(val logger: Logger, envDir: File, maxDbSize: Int, maxReaders: Int) : Store {
  private val env: EnvB
  private val dirty: DbiB
  private val output: DbiB
  private val callReqs: DbiB
  private val callersOf: DbiB
  private val callersOfValues: DbiB
  private val pathReqs: DbiB
  private val requireesOf: DbiB
  private val requireesOfValues: DbiB
  private val pathGens: DbiB
  private val generatorOf: DbiB


  init {
    envDir.mkdirs()
    env = Env.create(DirectBufferProxy.PROXY_DB)
      .setMapSize(maxDbSize.toLong())
      .setMaxReaders(maxReaders)
      .setMaxDbs(10)
      .open(envDir, EnvFlags.MDB_NOSYNC, EnvFlags.MDB_NOMETASYNC)
    dirty = env.openDbi("dirty", DbiFlags.MDB_CREATE)
    output = env.openDbi("output", DbiFlags.MDB_CREATE)
    callReqs = env.openDbi("callReqs", DbiFlags.MDB_CREATE)
    callersOf = env.openDbi("callersOf", DbiFlags.MDB_CREATE, DbiFlags.MDB_DUPSORT)
    callersOfValues = env.openDbi("callersOfValues", DbiFlags.MDB_CREATE)
    pathReqs = env.openDbi("pathReqs", DbiFlags.MDB_CREATE)
    requireesOf = env.openDbi("requireesOf", DbiFlags.MDB_CREATE, DbiFlags.MDB_DUPSORT)
    requireesOfValues = env.openDbi("requireesOfValues", DbiFlags.MDB_CREATE)
    pathGens = env.openDbi("pathGens", DbiFlags.MDB_CREATE)
    generatorOf = env.openDbi("generatorOf", DbiFlags.MDB_CREATE)
  }

  override fun close() {
    env.close()
  }


  override fun readTxn(): StoreReadTxn {
    val txn = env.txnRead()
    return LMDBStoreTxn(env, txn, false, logger,
      dirtyDb = dirty,
      outputDb = output,
      callReqsDb = callReqs,
      callersOfDb = callersOf,
      callersOfValuesDb = callersOfValues,
      pathReqsDb = pathReqs,
      requireesOfDb = requireesOf,
      requireesOfValuesDb = requireesOfValues,
      pathGensDb = pathGens,
      generatorOfDb = generatorOf
    )
  }

  override fun writeTxn(): StoreWriteTxn {
    val txn = env.txnWrite()
    return LMDBStoreTxn(env, txn, true, logger,
      dirtyDb = dirty,
      outputDb = output,
      callReqsDb = callReqs,
      callersOfDb = callersOf,
      callersOfValuesDb = callersOfValues,
      pathReqsDb = pathReqs,
      requireesOfDb = requireesOf,
      requireesOfValuesDb = requireesOfValues,
      pathGensDb = pathGens,
      generatorOfDb = generatorOf
    )
  }

  override fun sync() {
    env.sync(false)
  }


  override fun toString(): String {
    return "LMDBStore"
  }
}

internal open class LMDBStoreTxnBase(
  protected val env: EnvB,
  protected val txn: TxnB,
  private val isWriteTxn: Boolean,
  protected val logger: Logger
) {
  /// Serialization
  protected fun <T : Serializable?> serialize(obj: T, maxKeySize: Int? = null): Buf {
    return if(maxKeySize != null) {
      // TODO: always hashing when maxKeySize is set, but we could instead only hash when key exceeds the key size?
      serializeHashed(obj)
    } else {
      serializeToBytes(obj)
    }
  }

  data class SerializedAndHashed(val serialized: Buf, val hashed: Buf)

  protected fun <T : Serializable?> serializeAndHash(obj: T): SerializedAndHashed {
    val stream = ExpandableDirectBufferOutputStream(ExpandableDirectByteBuffer(1024))
    val digester = MessageDigest.getInstance("SHA-256")
    stream.use {
      // Internally digests to digester, and then passes written bytes to the expandable buffer stream.
      DigestOutputStream(it, digester).use {
        ObjectOutputStream(it).use {
          it.writeObject(obj)
        }
      }
    }
    val serialized = UnsafeBuffer(stream.buffer(), 0, stream.position())
    val digested = digester.digest()
    val hashed = UnsafeBuffer(ByteBuffer.allocateDirect(digested.size))
    hashed.putBytes(0, digested)
    return SerializedAndHashed(serialized, hashed)
  }

  private fun <T : Serializable?> serializeHashed(obj: T): Buf {
    val digester = MessageDigest.getInstance("SHA-256")
    DigestingOutputStream(digester).use {
      ObjectOutputStream(it).use {
        it.writeObject(obj)
      }
    }
    val digested = digester.digest()
    val buf = UnsafeBuffer(ByteBuffer.allocateDirect(digested.size))
    buf.putBytes(0, digested)
    return buf
  }

  private fun <T : Serializable?> serializeToBytes(obj: T): Buf {
    val stream = ExpandableDirectBufferOutputStream(ExpandableDirectByteBuffer(1024))
    stream.use {
      ObjectOutputStream(it).use {
        it.writeObject(obj)
      }
    }
    return UnsafeBuffer(stream.buffer(), 0, stream.position())
  }

  private fun emptyBuffer(): Buf {
    return UnsafeBuffer()
  }

  /// Deserialization
  private fun <T : Serializable?> deserialize(buffer: Buf): DeserializedOrDeleted<T> {
    DirectBufferInputStream(buffer).use {
      ObjectInputStream(it).use {
        return try {
          @Suppress("UNCHECKED_CAST")
          val deserialized = it.readObject() as T
          DeserializedOrDeleted(deserialized)
        } catch(e: ClassNotFoundException) {
          logger.error("Deserialization failed", e)
          DeserializedOrDeleted<T>()
        } catch(e: IOException) {
          logger.error("Deserialization failed", e)
          DeserializedOrDeleted<T>()
        }
      }
    }
  }

  /// Copying buffer
  protected fun copyBuffer(buffer: Buf): Buf {
    val newBuffer = UnsafeBuffer(ByteBuffer.allocateDirect(buffer.capacity()))
    newBuffer.putBytes(0, buffer, 0, buffer.capacity())
    return newBuffer
  }


  /// Getting data
  protected fun <T : Serializable> getBool(input: T, db: DbiB): Boolean {
    val key = serialize(input, env.maxKeySize)
    val value = db.get(txn, key)
    return value != null
  }


  protected fun <T : Serializable, R : Serializable?> getOne(input: T, db: DbiB): DeserializedOrDeleted<R>? {
    val key = serialize(input, env.maxKeySize)
    val value = db.get(txn, key) ?: return null
    return deserializeOrDelete<R>(key, value, db)
  }

  protected fun <R : Serializable?> getOne(key: Buf, db: DbiB): DeserializedOrDeleted<R>? {
    val value = db.get(txn, key) ?: return null
    return deserializeOrDelete<R>(key, value, db)
  }


  protected fun <T : Serializable, R : Serializable?> getMultiple(input: T, dbDup: DbiB, dbVal: DbiB): Set<R> {
    val key = serialize(input, env.maxKeySize)
    val hashedValues = ArrayList<Buf>()
    dbDup.openCursor(txn).use { cursor ->
      if(!cursor.get(key, GetOp.MDB_SET)) {
        return setOf()
      }
      do {
        val hashedValue = cursor.`val`()
        val hashedValueCopy = copyBuffer(hashedValue)
        hashedValues.add(hashedValueCopy)
      } while(cursor.seek(SeekOp.MDB_NEXT_DUP))
    }
    val results = HashSet<R>(hashedValues.size)
    for(hashedValue in hashedValues) {
      val value = dbVal.get(txn, hashedValue) ?: continue
      val deserializedValue = deserializeOrDelete<R>(hashedValue, value, dbVal)
      if(!deserializedValue.deleted) {
        results.add(deserializedValue.deserialized)
      } else {
        // Also delete key-value pair from dbDup when value could not be deserialized.
        if(isWriteTxn) {
          dbDup.delete(txn, key, hashedValue)
        } else {
          env.txnWrite().use {
            dbDup.delete(it, key, hashedValue)
          }
        }
      }
    }
    return results
  }

  private fun <R : Serializable?> deserializeOrDelete(key: Buf, value: Buf, db: DbiB): DeserializedOrDeleted<R> {
    val deserialized = deserialize<R>(value)
    if(deserialized.deleted) {
      if(isWriteTxn) {
        db.delete(txn, key)
      } else {
        env.txnWrite().use {
          db.delete(it, key)
        }
      }
    }
    return deserialized
  }


  /// Setting data
  protected fun <K : Serializable> setBool(input: K, result: Boolean, db: DbiB): Boolean {
    val key = serialize(input, env.maxKeySize)
    return if(result) {
      val value = emptyBuffer()
      db.put(txn, key, value)
    } else {
      db.delete(txn, key)
    }
  }


  protected fun <K : Serializable, V : Serializable?> setOne(input: K, result: V, db: DbiB): Boolean {
    val key = serialize(input, env.maxKeySize)
    return setOne(key, result, db)
  }

  protected fun <V : Serializable?> setOne(key: Buf, result: V, db: DbiB): Boolean {
    val value = serialize(result)
    return setOne(key, value, db)
  }

  protected fun <K : Serializable> setOne(input: K, value: Buf, db: DbiB): Boolean {
    val key = serialize(input, env.maxKeySize)
    return setOne(key, value, db)
  }

  protected fun setOne(key: Buf, value: Buf, db: DbiB): Boolean {
    return db.put(txn, key, value)
  }


  protected fun <K : Serializable> deleteOne(input: K, db: DbiB): Boolean {
    val key = serialize(input, env.maxKeySize)
    return deleteOne(key, db)
  }

  protected fun deleteOne(key: Buf, db: DbiB): Boolean {
    return db.delete(txn, key)
  }


  protected fun <K : Serializable, V : Serializable?> setDup(input: K, result: V, dbDup: DbiB, dbVal: DbiB): Boolean {
    val key = serialize(input, env.maxKeySize)
    val (value, hashedValue) = serializeAndHash(result)
    return setDup(key, value, hashedValue, dbDup, dbVal)
  }

  protected fun <V : Serializable?> setDup(key: Buf, result: V, dbDup: DbiB, dbVal: DbiB): Boolean {
    val (value, hashedValue) = serializeAndHash(result)
    return setDup(key, value, hashedValue, dbDup, dbVal)
  }

  protected fun <K : Serializable> setDup(input: K, value: Buf, hashedValue: Buf, dbDup: DbiB, dbVal: DbiB): Boolean {
    val key = serialize(input, env.maxKeySize)
    return setDup(key, value, hashedValue, dbDup, dbVal)
  }

  protected fun setDup(key: Buf, value: Buf, hashedValue: Buf, dbDup: DbiB, dbVal: DbiB): Boolean {
    val put1 = dbDup.put(txn, key, hashedValue, PutFlags.MDB_NODUPDATA)
    val put2 = dbVal.put(txn, hashedValue, value)
    return put1 && put2
  }


  protected fun <K : Serializable, V : Serializable?> deleteDup(input: K, result: V, dbDup: DbiB, dbVal: DbiB): Boolean {
    val key = serialize(input, env.maxKeySize)
    return deleteDup(key, result, dbDup, dbVal)
  }

  protected fun <V : Serializable?> deleteDup(key: Buf, result: V, dbDup: DbiB, dbVal: DbiB): Boolean {
    val hashedValue = serialize(result, env.maxKeySize)
    return deleteDup(key, hashedValue, dbDup, dbVal)
  }

  protected fun <K : Serializable> deleteDup(input: K, hashedValue: Buf, dbDup: DbiB, dbVal: DbiB): Boolean {
    val key = serialize(input, env.maxKeySize)
    return deleteDup(key, hashedValue, dbDup, dbVal)
  }

  protected fun deleteDup(key: Buf, hashedValue: Buf, dbDup: DbiB, @Suppress("UNUSED_PARAMETER") dbVal: DbiB): Boolean {
    return dbDup.delete(txn, key, hashedValue)
    // PROB: cannot delete (hashedValue, value) from value database, since multiple entries from dbDup may refer to it.
  }
}

internal open class LMDBStoreTxn(
  env: EnvB,
  txn: TxnB,
  isWriteTxn: Boolean,
  logger: Logger,
  private val dirtyDb: DbiB,
  private val outputDb: DbiB,
  private val callReqsDb: DbiB,
  private val callersOfDb: DbiB,
  private val callersOfValuesDb: DbiB,
  private val pathReqsDb: DbiB,
  private val requireesOfDb: DbiB,
  private val requireesOfValuesDb: DbiB,
  private val pathGensDb: DbiB,
  private val generatorOfDb: DbiB
) : StoreReadTxn, StoreWriteTxn, LMDBStoreTxnBase(env, txn, isWriteTxn, logger) {
  override fun numSourceFiles(): Int {
    TODO("implement numSourceFiles in LMDBStoreTxn")
  }

  override fun dirty(app: UFuncApp): Boolean {
    return getBool(app, dirtyDb)
  }

  override fun output(app: UFuncApp): UOutput? {
    return getOne<UFuncApp, Out>(app, outputDb).mapOrElse(null) { value ->
      Output(value)
    }
  }

  override fun callReqs(app: UFuncApp): ArrayList<CallReq> {
    return getOne<UFuncApp, ArrayList<CallReq>>(app, callReqsDb).orElse(arrayListOf())
  }

  override fun callersOf(app: UFuncApp): Set<UFuncApp> {
    return getMultiple(app, callersOfDb, callersOfValuesDb)
  }

  override fun pathReqs(app: UFuncApp): ArrayList<PathReq> {
    return getOne<UFuncApp, ArrayList<PathReq>>(app, pathReqsDb).orElse(arrayListOf())
  }

  override fun requireesOf(path: PPath): Set<UFuncApp> {
    return getMultiple(path, requireesOfDb, requireesOfValuesDb)
  }

  override fun pathGens(app: UFuncApp): ArrayList<PathGen> {
    return getOne<UFuncApp, ArrayList<PathGen>>(app, pathGensDb).orElse(arrayListOf())
  }

  override fun generatorOf(path: PPath): UFuncApp? {
    return getOne<PPath, UFuncApp?>(path, generatorOfDb).orElse(null)
  }

  override fun data(app: UFuncApp): UFuncAppData? {
    // TODO: buffer copies required?
    val appKey = serialize(app, env.maxKeySize)
    val outputDeserialized = getOne<Out>(copyBuffer(appKey), outputDb)
    if(outputDeserialized == null || outputDeserialized.deleted) {
      return null
    }
    val output = outputDeserialized.deserialized
    val callReqs = getOne<ArrayList<CallReq>>(copyBuffer(appKey), callReqsDb).orElse(arrayListOf())
    val pathReqs = getOne<ArrayList<PathReq>>(copyBuffer(appKey), pathReqsDb).orElse(arrayListOf())
    val pathGens = getOne<ArrayList<PathGen>>(appKey, pathGensDb).orElse(arrayListOf())
    return FuncAppData(output, callReqs, pathReqs, pathGens)
  }


  override fun setDirty(app: UFuncApp, isDirty: Boolean) {
    setBool(app, isDirty, dirtyDb)
  }

  override fun setOutput(app: UFuncApp, output: Out) {
    setOne(app, output, outputDb)
  }

  override fun setCallReqs(app: UFuncApp, callReqs: ArrayList<CallReq>) {
    logger.trace("Setting call reqs of ${app.toShortString(100)} to $callReqs")
    // TODO: buffer copies required?
    val (callerAppVal, callerAppKey) = serializeAndHash(app)
    // Remove old inverse call requirements.
    logger.trace(" * removing old inverse callers")
    val oldCallReqs = getOne<ArrayList<CallReq>>(copyBuffer(callerAppKey), callReqsDb).orElse(arrayListOf())
    for(oldCallReq in oldCallReqs) {
      logger.trace("   * removing: ${oldCallReq.callee.toShortString(50)} -> ${app.toShortString(50)}")
      val deleted = deleteDup(oldCallReq.callee, copyBuffer(callerAppKey), callersOfDb, callersOfValuesDb)
      logger.trace("   * deletion success: $deleted")
    }
    // OPTO: diff callReqs and oldCallReqs, remove/add entries based on diff.
    // Add new call requirements.
    logger.trace(" * setting call requirements")
    val set = setOne(copyBuffer(callerAppKey), callReqs, callReqsDb)
    logger.trace(" * setting success: $set")
    logger.trace(" * adding new inverse callers")
    for(callReq in callReqs) {
      logger.trace("   * adding: ${callReq.callee.toShortString(50)} -> ${app.toShortString(50)}")
      val added = setDup(callReq.callee, copyBuffer(callerAppVal), copyBuffer(callerAppKey), callersOfDb, callersOfValuesDb)
      logger.trace("   * addition success: $added")
    }
  }

  override fun setPathReqs(app: UFuncApp, pathReqs: ArrayList<PathReq>) {
    // TODO: buffer copies required?
    val (appValue, appKey) = serializeAndHash(app)
    // Remove old inverse path requirements.
    val oldPathReqs = getOne<ArrayList<PathReq>>(copyBuffer(appKey), pathReqsDb).orElse(arrayListOf())
    for(oldPathReq in oldPathReqs) {
      deleteDup(oldPathReq.path, copyBuffer(appKey), requireesOfDb, requireesOfValuesDb)
    }
    // OPTO: diff pathReqs and oldPathReqs, remove/add entries based on diff.
    // Add new path requirements.
    setOne(copyBuffer(appKey), pathReqs, pathReqsDb)
    for(pathReq in pathReqs) {
      setDup(pathReq.path, copyBuffer(appValue), copyBuffer(appKey), requireesOfDb, requireesOfValuesDb)
    }
  }

  override fun setPathGens(app: UFuncApp, pathGens: ArrayList<PathGen>) {
    // TODO: buffer copies required?
    val (appValue, appKey) = serializeAndHash(app)
    // Remove old inverse path generates.
    val oldPathGens = getOne<ArrayList<PathGen>>(copyBuffer(appKey), pathGensDb).orElse(arrayListOf())
    for(oldPathGen in oldPathGens) {
      deleteOne(oldPathGen.path, generatorOfDb)
    }
    // OPTO: diff pathGens and oldPathGens, remove/add entries based on diff.
    // Add new path generates.
    setOne(appKey, pathGens, pathGensDb)
    for(pathGen in pathGens) {
      setOne(pathGen.path, copyBuffer(appValue), generatorOfDb)
    }
  }

  override fun setData(app: UFuncApp, data: UFuncAppData) {
    // OPTO: serialize and hash app only once
    setOutput(app, data.output)
    setCallReqs(app, data.callReqs)
    setPathReqs(app, data.pathReqs)
    setPathGens(app, data.pathGens)
  }

  override fun drop() {
    dirtyDb.drop(txn)
    outputDb.drop(txn)
    callReqsDb.drop(txn)
    callersOfDb.drop(txn)
    callersOfValuesDb.drop(txn)
    pathReqsDb.drop(txn)
    requireesOfDb.drop(txn)
    requireesOfValuesDb.drop(txn)
    pathGensDb.drop(txn)
    generatorOfDb.drop(txn)
  }


  override fun close() {
    txn.commit()
  }
}


class DigestingOutputStream(private val digest: MessageDigest) : OutputStream() {
  @Throws(IOException::class)
  override fun write(b: Int) {
    digest.update(b.toByte())
  }

  @Throws(IOException::class)
  override fun write(b: ByteArray, off: Int, len: Int) {
    digest.update(b, off, len)
  }
}


data class DeserializedOrDeleted<out R : Serializable?>(val deserialized: R, val deleted: Boolean) {
  constructor(deserialized: R) : this(deserialized, false)
  @Suppress("UNCHECKED_CAST")
  constructor() : this(null as R, true)
}

fun <R : Serializable?> DeserializedOrDeleted<R>?.orElse(default: R): R {
  return if(this == null || this.deleted) {
    default
  } else {
    this.deserialized
  }
}


fun <R : Serializable?, RR> DeserializedOrDeleted<R>?.mapOrElse(default: RR, func: (R) -> RR): RR {
  return if(this == null || this.deleted) {
    default
  } else {
    func(this.deserialized)
  }
}
