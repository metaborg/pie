package mb.pie.runtime.core.impl.store

import com.google.inject.Inject
import mb.log.Logger
import mb.pie.runtime.core.*
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
  private val results: DbiB
  private val called: DbiB
  private val calledValues: DbiB
  private val required: DbiB
  private val requiredValues: DbiB
  private val generated: DbiB


  init {
    envDir.mkdirs()
    env = Env.create(DirectBufferProxy.PROXY_DB)
      .setMapSize(maxDbSize.toLong())
      .setMaxReaders(maxReaders)
      .setMaxDbs(7)
      .open(envDir, EnvFlags.MDB_NOSYNC, EnvFlags.MDB_NOMETASYNC)
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

  override fun sync() {
    env.sync(false)
  }


  override fun toString(): String {
    return "LMDBStore"
  }
}

open internal class LMDBStoreTxnBase(
  private val env: EnvB,
  protected val txn: TxnB,
  private val isWriteTxn: Boolean,
  protected val logger: Logger
) {
  /// Serialization
  private fun <T : Serializable> serialize(obj: T, maxKeySize: Int? = null): Buf {
    return if(maxKeySize != null) {
      // TODO: always hashing when maxKeySize is set, but we could instead only hash when key exceeds the key size?
      serializeHashed(obj)
    } else {
      serializeToBytes(obj)
    }
  }

  data class SerializedAndHashed(val serialized: Buf, val hashed: Buf)

  private fun <T : Serializable> serializeAndHash(obj: T): SerializedAndHashed {
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

  private fun <T : Serializable> serializeHashed(obj: T): Buf {
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

  private fun <T : Serializable> serializeToBytes(obj: T): Buf {
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
  private fun <T : Serializable> deserialize(buffer: Buf): T? {
    DirectBufferInputStream(buffer).use {
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
  private fun copyBuffer(buffer: Buf): Buf {
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

  protected fun <T : Serializable, R : Serializable> getOne(input: T, db: DbiB): R? {
    val key = serialize(input, env.maxKeySize)
    val value = db.get(txn, key) ?: return null
    return deserializeOrDelete(key, value, db)
  }

  protected fun <T : Serializable, R : Serializable> getMultiple(input: T, dbDup: DbiB, dbVal: DbiB): Set<R> {
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

  private fun <R : Serializable> deserializeOrDelete(key: Buf, value: Buf, db: DbiB): R? {
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
    val (value, hashedValue) = serializeAndHash(result)
    val put1 = dbDup.put(txn, key, hashedValue, PutFlags.MDB_NODUPDATA)
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

  override fun resultOf(app: UFuncApp): UExecRes? {
    return getOne(app, resultsDb)
  }

  override fun callersOf(callee: UFuncApp): Set<UFuncApp> {
    return getMultiple(callee, calledDb, calledValuesDb)
  }

  override fun requireesOf(path: PPath): Set<UFuncApp> {
    return getMultiple(path, requiredDb, requiredValuesDb)
  }

  override fun generatorOf(path: PPath): UFuncApp? {
    return getOne(path, generatedDb)
  }


  override fun setIsDirty(app: UFuncApp, isDirty: Boolean) {
    setBool(app, isDirty, dirtyDb)
  }

  override fun setResultOf(app: UFuncApp, result: UExecRes) {
    setOne(app, result, resultsDb)
  }

  override fun setCallerOf(caller: UFuncApp, callee: UFuncApp) {
    setDup(callee, caller, calledDb, calledValuesDb)
  }

  override fun setRequireeOf(requiree: UFuncApp, path: PPath) {
    setDup(path, requiree, requiredDb, requiredValuesDb)
  }

  override fun setGeneratorOf(generator: UFuncApp, path: PPath) {
    setOne(path, generator, generatedDb)
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
