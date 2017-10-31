package mb.pie.runtime.core.impl.store

import com.google.inject.Inject
import mb.log.Logger
import mb.pie.runtime.core.*
import mb.vfs.path.PPath
import org.lmdbjava.*
import java.io.*
import java.nio.ByteBuffer
import java.security.MessageDigest


typealias EnvB = Env<ByteBuffer>
typealias DbiB = Dbi<ByteBuffer>
typealias TxnB = Txn<ByteBuffer>

class LMDBBuildStoreFactory @Inject constructor(val logger: Logger) {
  fun create(envDir: File, maxDbSize: Int = 1024 * 1024 * 1024, maxReaders: Int = 1024): LMDBStore {
    return LMDBStore(logger.forContext(LMDBStore::class.java), envDir, maxDbSize, maxReaders)
  }
}

class LMDBStore(val logger: Logger, envDir: File, maxDbSize: Int, maxReaders: Int) : Store {
  private val env: EnvB
  private val dirty: DbiB
  private val results: DbiB
  private val called: DbiB
  private val calledValues: DbiB
  private val required: DbiB
  private val requiredValues: DbiB
  private val generated: DbiB


  init {
    envDir.mkdirs()
    env = Env.create().setMapSize(maxDbSize.toLong()).setMaxReaders(maxReaders).setMaxDbs(7).open(envDir)
    dirty = env.openDbi("dirty", DbiFlags.MDB_CREATE)
    results = env.openDbi("results", DbiFlags.MDB_CREATE)
    called = env.openDbi("called", DbiFlags.MDB_CREATE, DbiFlags.MDB_DUPSORT)
    calledValues = env.openDbi("calledValues", DbiFlags.MDB_CREATE)
    required = env.openDbi("required", DbiFlags.MDB_CREATE, DbiFlags.MDB_DUPSORT)
    requiredValues = env.openDbi("requiredValues", DbiFlags.MDB_CREATE)
    generated = env.openDbi("generated", DbiFlags.MDB_CREATE)
  }

  override fun close() {
    env.close()
  }


  override fun readTxn(): StoreReadTxn {
    val txn = env.txnRead()
    return LMDBStoreTxn(env, txn, false, logger, dirty, results, called, calledValues, required, requiredValues, generated)
  }

  override fun writeTxn(): StoreWriteTxn {
    val txn = env.txnWrite()
    return LMDBStoreTxn(env, txn, true, logger, dirty, results, called, calledValues, required, requiredValues, generated)
  }


  override fun toString(): String {
    return "LMDBStore"
  }
}

open internal class LMDBStoreTxnBase(
  protected val env: EnvB,
  protected val txn: TxnB,
  protected val isWriteTxn: Boolean,
  protected val logger: Logger
) {
  /// Serialization
  private fun <T : Serializable> serialize(obj: T, maxKeySize: Int? = null): ByteBuffer {
    // TODO: copies the bytes: not efficient
    val bytes = serializeToBytes(obj)
    if(maxKeySize != null && bytes.size > maxKeySize) {
      return hash(bytes)
    }
    // TODO: this copies bytes again: not efficient
    val buffer = ByteBuffer.allocateDirect(bytes.size)
    buffer.put(bytes).flip()
    return buffer
  }

  private fun <T : Serializable> serializeHashed(obj: T): ByteBuffer {
    // TODO: copies the bytes: not efficient
    val bytes = serializeToBytes(obj)
    return hash(bytes)
  }

  private fun <T : Serializable> serializeToBytes(obj: T): ByteArray {
    ByteArrayOutputStream().use {
      ObjectOutputStream(it).use {
        it.writeObject(obj)
      }
      // TODO: ObjectOutputStream.toByteArray() copies the bytes: not efficient
      return it.toByteArray()
    }
  }

  private fun emptyBuffer(): ByteBuffer {
    return ByteBuffer.allocateDirect(0)
  }


  /// Deserialization
  private fun <T : Serializable> deserialize(buffer: ByteBuffer): T? {
    ByteBufferBackedInputStream(buffer).use {
      ObjectInputStream(it).use {
        try {
          @Suppress("UNCHECKED_CAST")
          return it.readObject() as T
        } catch(e: ClassNotFoundException) {
          logger.error("Deserialization failed", e)
          return null
        } catch(e: ObjectStreamException) {
          logger.error("Deserialization failed", e)
          return null
        } catch(e: IOException) {
          logger.error("Deserialization failed", e)
          return null
        }
      }
    }
  }


  /// Copying buffer
  private fun copyBuffer(buffer: ByteBuffer): ByteBuffer {
    val readOnlyBuffer = buffer.asReadOnlyBuffer()
    val newBuffer = ByteBuffer.allocateDirect(readOnlyBuffer.capacity())
    readOnlyBuffer.rewind()
    newBuffer.put(readOnlyBuffer)
    newBuffer.flip()
    return newBuffer
  }


  /// Hashing buffer
  private fun hash(bytes: ByteArray): ByteBuffer {
    val digest = MessageDigest.getInstance("SHA-1")
    val digestBytes = digest.digest(bytes)
    // TODO: this copies bytes again: not efficient
    val buffer = ByteBuffer.allocateDirect(digestBytes.size)
    buffer.put(digestBytes).flip()
    return buffer
  }


  /// Getting data
  protected fun <T : Serializable> getBool(input: T, db: DbiB): Boolean {
    val key = serialize(input, env.maxKeySize)
    val value = db.get(txn, key)
    return value != null
  }

  protected fun <T : Serializable, R : Serializable> getOne(input: T, db: DbiB): R? {
    val key = serialize(input, env.maxKeySize)
    val value = db.get(txn, key) ?: return null
    return deserializeOrDelete<T, R>(key, value, db)
  }

  protected fun <T : Serializable, R : Serializable> getMultiple(input: T, dbDup: DbiB, dbVal: DbiB): Set<R> {
    val key = serialize(input, env.maxKeySize)
    val hashedValues = ArrayList<ByteBuffer>()
    dbDup.openCursor(txn).use { cursor ->
      // TODO: use GetOp.MDB_SET since we do not look at the key at all?
      if(!cursor.get(key, GetOp.MDB_SET_KEY)) {
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
      val deserializedValue = deserializeOrDelete<T, R>(hashedValue, value, dbVal)
      if(deserializedValue != null) {
        results.add(deserializedValue)
      } else {
        // Also delete key-value pair from dbDup when value could not be deserialized.
        if(isWriteTxn) {
          dbDup.delete(txn, key)
        } else {
          env.txnWrite().use {
            dbDup.delete(it, key, hashedValue)
          }
        }
      }
    }
    return results
  }

  private fun <T : Serializable, R : Serializable> deserializeOrDelete(key: ByteBuffer, value: ByteBuffer, db: DbiB): R? {
    val deserialized = deserialize<R>(value)
    return if(deserialized != null) {
      deserialized
    } else {
      if(isWriteTxn) {
        db.delete(txn, key)
      } else {
        env.txnWrite().use {
          db.delete(it, key)
        }
      }
      null
    }
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

  protected fun <K : Serializable, V : Serializable> setOne(input: K, result: V, db: DbiB): Boolean {
    val key = serialize(input, env.maxKeySize)
    val value = serialize(result)
    return db.put(txn, key, value)
  }

  protected fun <K : Serializable, V : Serializable> setDup(input: K, result: V, dbDup: DbiB, dbVal: DbiB): Boolean {
    val key = serialize(input, env.maxKeySize)
    // TODO: serializing twice, not efficient.
    val hashedValue = serializeHashed(result)
    val value = serialize(result)
    // TODO: use PutFlags.MDB_NODUPDATA to prevent storing duplicate key-value pairs?
    val put1 = dbDup.put(txn, key, hashedValue)
    val put2 = dbVal.put(txn, hashedValue, value)
    return put1 && put2
  }
}

open internal class LMDBStoreTxn(
  env: EnvB,
  txn: TxnB,
  isWriteTxn: Boolean,
  logger: Logger,
  private val dirtyDb: DbiB,
  private val resultsDb: DbiB,
  private val calledDb: DbiB,
  private val calledValuesDb: DbiB,
  private val requiredDb: DbiB,
  private val requiredValuesDb: DbiB,
  private val generatedDb: DbiB
) : StoreReadTxn, StoreWriteTxn, LMDBStoreTxnBase(env, txn, isWriteTxn, logger) {
  override fun isDirty(app: UFuncApp): Boolean {
    return getBool(app, dirtyDb)
  }

  override fun resultsIn(app: UFuncApp): UExecRes? {
    return getOne(app, resultsDb)
  }

  override fun calledBy(app: UFuncApp): Set<UFuncApp> {
    return getMultiple(app, calledDb, calledValuesDb)
  }

  override fun requiredBy(path: PPath): Set<UFuncApp> {
    return getMultiple(path, requiredDb, requiredValuesDb)
  }

  override fun generatedBy(path: PPath): UFuncApp? {
    return getOne(path, generatedDb)
  }


  override fun setIsDirty(app: UFuncApp, isDirty: Boolean) {
    setBool(app, isDirty, dirtyDb)
  }

  override fun setResultsIn(app: UFuncApp, resultsIn: UExecRes) {
    setOne(app, resultsIn, resultsDb)
  }

  override fun setCalledBy(app: UFuncApp, calledBy: UFuncApp) {
    setDup(app, calledBy, calledDb, calledValuesDb)
  }

  override fun setRequiredBy(path: PPath, requiredBy: UFuncApp) {
    setDup(path, requiredBy, requiredDb, requiredValuesDb)
  }

  override fun setGeneratedBy(path: PPath, generatedBy: UFuncApp) {
    setOne(path, generatedBy, generatedDb)
  }

  override fun drop() {
    dirtyDb.drop(txn)
    resultsDb.drop(txn)
    calledDb.drop(txn)
    calledValuesDb.drop(txn)
    requiredDb.drop(txn)
    requiredValuesDb.drop(txn)
    generatedDb.drop(txn)
  }


  override fun close() {
    txn.commit()
  }
}


internal class ByteBufferBackedInputStream(private val buf: ByteBuffer) : InputStream() {
  @Throws(IOException::class)
  override fun read(): Int {
    if(!buf.hasRemaining()) {
      return -1
    }
    return buf.get().toInt() and 0xFF
  }

  @Throws(IOException::class)
  override fun read(bytes: ByteArray, offset: Int, length: Int): Int {
    if(!buf.hasRemaining()) {
      return -1
    }

    val minLength = Math.min(length, buf.remaining())
    buf.get(bytes, offset, minLength)
    return minLength
  }
}
