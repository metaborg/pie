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
    return LMDBStoreReadTxn(env, dirty, results, called, calledValues, required, requiredValues, generated, txn, logger)
  }

  override fun writeTxn(): StoreWriteTxn {
    val txn = env.txnWrite()
    return LMDBStoreWriteTxn(env, dirty, results, called, calledValues, required, requiredValues, generated, txn, logger)
  }


  override fun toString(): String {
    return "LMDBStore"
  }
}

interface LMDBStoreTxnTrait {
  fun <T : Serializable> serialize(obj: T, maxKeySize: Int? = null): ByteBuffer {
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

  fun <T : Serializable> serializeHashed(obj: T): ByteBuffer {
    // TODO: copies the bytes: not efficient
    val bytes = serializeToBytes(obj)
    return hash(bytes)
  }

  fun emptyBuffer(): ByteBuffer {
    return ByteBuffer.allocateDirect(0)
  }


  fun <T : Serializable> deserialize(buffer: ByteBuffer, logger: Logger): T? {
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


  private fun hash(bytes: ByteArray): ByteBuffer {
    val digest = MessageDigest.getInstance("SHA-1")
    val digestBytes = digest.digest(bytes)
    // TODO: this copies bytes again: not efficient
    val buffer = ByteBuffer.allocateDirect(digestBytes.size)
    buffer.put(digestBytes).flip()
    return buffer
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
}

open internal class LMDBStoreReadTxn(
  protected val env: EnvB,
  protected val dirty: DbiB,
  protected val results: DbiB,
  protected val called: DbiB,
  protected val calledValues: DbiB,
  protected val required: DbiB,
  protected val requiredValues: DbiB,
  protected val generated: DbiB,
  protected val txn: TxnB,
  protected val logger: Logger
) : LMDBStoreTxnTrait, StoreReadTxn {
  override fun isDirty(app: UFuncApp): Boolean {
    val keyBytes = serialize(app, env.maxKeySize)
    val result = dirty.get(txn, keyBytes)
    return result != null
  }

  override fun resultsIn(app: UFuncApp): UExecRes? {
    val key = serialize(app, env.maxKeySize)
    val value = results.get(txn, key)
    if(value != null) {
      val deserialized = deserialize<UExecRes>(value, logger)
      if(deserialized != null) {
        return deserialized
      }
      // Deserialization failed, remove entry
      logger.error("Cannot get produced value for $app, deserialization failed")
      results.delete(key)
    }
    return null
  }

  override fun calledBy(app: UFuncApp): Set<UFuncApp> {
    val key = serialize(app, env.maxKeySize)
    called.openCursor(txn).use { cursor ->
      if(!cursor.get(key, GetOp.MDB_SET)) {
        return setOf()
      }
      val apps = HashSet<UFuncApp>()
      val numValues = cursor.count()
      for(i in 0 until numValues) {
        val hashedVal = cursor.`val`()
        // TODO: can we safely get from another database while iterating the cursor? if not, need to copy buffers and get results in another loop.
        val actualVal = calledValues.get(txn, hashedVal)
        if(actualVal == null) {
          logger.error("Could not find called function application for hash $hashedVal")
        } else {
          val deserialized = deserialize<UFuncApp>(actualVal, logger)
          if(deserialized != null) {
            apps.add(deserialized)
          } else {
            // Deserialization failed, remove entry
            logger.error("Cannot get called by value for $app, deserialization failed")
            // TODO: can we safely delete from another database while iterating the cursor?
            calledValues.delete(hashedVal)
          }
        }
        cursor.next()
      }
      return apps
    }
  }

  override fun requiredBy(path: PPath): Set<UFuncApp> {
    val key = serialize(path, env.maxKeySize)
    required.openCursor(txn).use { cursor ->
      if(!cursor.get(key, GetOp.MDB_SET)) {
        return setOf()
      }
      val apps = HashSet<UFuncApp>()
      val numValues = cursor.count()
      for(i in 0 until numValues) {
        val hashedVal = cursor.`val`()
        // TODO: can we safely get from another database while iterating the cursor? if not, need to copy buffers and get results in another loop.
        val actualVal = requiredValues.get(txn, hashedVal)
        if(actualVal == null) {
          logger.error("Could not find required function application for hash $hashedVal")
        } else {
          val deserialized = deserialize<UFuncApp>(actualVal, logger)
          if(deserialized != null) {
            apps.add(deserialized)
          } else {
            // Deserialization failed, remove entry
            logger.error("Cannot get required by value for $path, deserialization failed")
            // TODO: can we safely delete from another database while iterating the cursor?
            requiredValues.delete(hashedVal)
          }
        }
        cursor.next()
      }
      return apps
    }
  }

  override fun generatedBy(path: PPath): UFuncApp? {
    val key = serialize(path, env.maxKeySize)
    val value = generated.get(txn, key)
    if(value != null) {
      val deserialized = deserialize<UFuncApp>(value, logger)
      if(deserialized != null) {
        return deserialized
      }
      // Deserialization failed, remove entry
      logger.error("Cannot get generated by value for $path, deserialization failed")
      generated.delete(key)
    }
    return null
  }

  override fun close() {
    txn.abort()
  }
}

internal class LMDBStoreWriteTxn(
  env: EnvB,
  dirty: DbiB,
  results: DbiB,
  called: DbiB,
  calledValues: DbiB,
  required: DbiB,
  requiredValues: DbiB,
  generated: DbiB,
  txn: TxnB,
  logger: Logger
) : LMDBStoreTxnTrait, StoreWriteTxn, LMDBStoreReadTxn(env, dirty, results, called, calledValues, required, requiredValues, generated, txn, logger) {
  override fun setIsDirty(app: UFuncApp, isDirty: Boolean) {
    val k = serialize(app, env.maxKeySize)
    if(isDirty) {
      val v = emptyBuffer()
      dirty.put(txn, k, v)
    } else {
      dirty.delete(txn, k)
    }
  }

  override fun setResultsIn(app: UFuncApp, resultsIn: UExecRes) {
    val key = serialize(app, env.maxKeySize)
    val value = serialize(resultsIn)
    results.put(txn, key, value)
  }

  override fun setCalledBy(app: UFuncApp, calledBy: UFuncApp) {
    val key = serialize(app, env.maxKeySize)
    // TODO: serializing twice, not efficient.
    val hashedValue = serializeHashed(calledBy)
    val value = serialize(calledBy)
    // TODO: use PutFlags.MDB_NODUPDATA to prevent storing duplicate key-value pairs?
    called.put(txn, key, hashedValue)
    calledValues.put(txn, hashedValue, value)
  }

  override fun setRequiredBy(path: PPath, requiredBy: UFuncApp) {
    val key = serialize(path, env.maxKeySize)
    // TODO: serializing twice, not efficient.
    val hashedValue = serializeHashed(requiredBy)
    val value = serialize(requiredBy)
    // TODO: use PutFlags.MDB_NODUPDATA to prevent storing duplicate key-value pairs?
    required.put(txn, key, hashedValue)
    requiredValues.put(txn, hashedValue, value)
  }

  override fun setGeneratedBy(path: PPath, generatedBy: UFuncApp) {
    val key = serialize(path, env.maxKeySize)
    val value = serialize(generatedBy)
    this.generated.put(txn, key, value)
  }

  override fun drop() {
    dirty.drop(txn)
    results.drop(txn)
    called.drop(txn)
    calledValues.drop(txn)
    required.drop(txn)
    requiredValues.drop(txn)
    generated.drop(txn)
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
