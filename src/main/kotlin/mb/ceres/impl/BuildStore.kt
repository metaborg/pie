package mb.ceres.internal

import mb.ceres.CPath
import mb.ceres.UBuildApp
import org.lmdbjava.Dbi
import org.lmdbjava.DbiFlags
import org.lmdbjava.Env
import java.io.*
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

interface BuildStore : AutoCloseable {
  fun setProduces(app: UBuildApp, res: UBuildRes)
  fun produces(app: UBuildApp): UBuildRes?

  fun setGeneratedBy(path: CPath, res: UBuildRes)
  fun generatedBy(path: CPath): UBuildRes?

  fun setRequiredBy(path: CPath, res: UBuildRes)
  fun requiredBy(path: CPath): UBuildRes?

  fun reset()
}

class InMemoryBuildStore : BuildStore {
  val produces = ConcurrentHashMap<UBuildApp, UBuildRes>()
  val generatedBy = ConcurrentHashMap<CPath, UBuildRes>()
  val requiredBy = ConcurrentHashMap<CPath, UBuildRes>()


  override fun setProduces(app: UBuildApp, res: UBuildRes) {
    produces[app] = res
  }

  override fun produces(app: UBuildApp): UBuildRes? {
    return produces[app]
  }


  override fun setGeneratedBy(path: CPath, res: UBuildRes) {
    generatedBy[path] = res
  }

  override fun generatedBy(path: CPath): UBuildRes? {
    return generatedBy[path]
  }


  override fun setRequiredBy(path: CPath, res: UBuildRes) {
    requiredBy[path] = res
  }

  override fun requiredBy(path: CPath): UBuildRes? {
    return requiredBy[path]
  }


  override fun reset() {
    produces.clear()
    generatedBy.clear()
  }

  override fun close() {}


  override fun toString(): String {
    return "InMemoryBuildStore"
  }
}

class LMDBBuildStore(envDir: File, maxDbSize: Int = 1024 * 1024 * 128, maxReaders: Int = 8) : BuildStore {
  val env: Env<ByteBuffer>
  val produces: Dbi<ByteBuffer>
  val generatedBy: Dbi<ByteBuffer>
  val requiredBy: Dbi<ByteBuffer>


  init {
    envDir.mkdirs()
    env = Env.create().setMapSize(maxDbSize.toLong()).setMaxReaders(maxReaders).setMaxDbs(3).open(envDir)
    produces = env.openDbi("produces", DbiFlags.MDB_CREATE)
    generatedBy = env.openDbi("generatedBy", DbiFlags.MDB_CREATE)
    requiredBy = env.openDbi("requiredBy", DbiFlags.MDB_CREATE)
  }

  override fun close() {
    env.close()
  }


  override fun setProduces(app: UBuildApp, res: UBuildRes) {
    val k = serialize(app, true)
    val v = serialize(res)
    env.txnWrite().use {
      produces.put(it, k, v)
      it.commit()
    }
  }

  override fun produces(app: UBuildApp): UBuildRes? {
    var keyBytes: ByteBuffer? = null
    var valBytes: ByteBuffer? = null
    env.txnRead().use {
      keyBytes = serialize(app, true)
      valBytes = produces.get(it, keyBytes)
    }
    if (valBytes != null) {
      val result = deserialize<UBuildRes>(valBytes!!)
      if (result != null) {
        return result
      }
      // Deserialization failed, remove entry
      produces.delete(keyBytes!!)
    }
    return null
  }


  override fun setGeneratedBy(path: CPath, res: UBuildRes) {
    val k = serialize(path, true)
    val v = serialize(res)
    env.txnWrite().use {
      generatedBy.put(it, k, v)
      it.commit()
    }
  }

  override fun generatedBy(path: CPath): UBuildRes? {
    var keyBytes: ByteBuffer? = null
    var valBytes: ByteBuffer? = null
    env.txnRead().use {
      keyBytes = serialize(path, true)
      valBytes = generatedBy.get(it, keyBytes)
    }
    if (valBytes != null) {
      val result = deserialize<UBuildRes>(valBytes!!)
      if (result != null) {
        return result
      }
      // Deserialization failed, remove entry
      generatedBy.delete(keyBytes!!)
    }
    return null
  }


  override fun setRequiredBy(path: CPath, res: UBuildRes) {
    val k = serialize(path, true)
    val v = serialize(res)
    env.txnWrite().use {
      requiredBy.put(it, k, v)
      it.commit()
    }
  }

  override fun requiredBy(path: CPath): UBuildRes? {
    var keyBytes: ByteBuffer? = null
    var valBytes: ByteBuffer? = null
    env.txnRead().use {
      keyBytes = serialize(path, true)
      valBytes = requiredBy.get(it, keyBytes)
    }
    if (valBytes != null) {
      val result = deserialize<UBuildRes>(valBytes!!)
      if (result != null) {
        return result
      }
      // Deserialization failed, remove entry
      requiredBy.delete(keyBytes!!)
    }
    return null
  }


  override fun reset() {
    env.txnWrite().use {
      produces.drop(it)
      generatedBy.drop(it)
      requiredBy.drop(it)
      it.commit()
    }
  }


  private fun <T> serialize(obj: T, isKey: Boolean = false): ByteBuffer {
    ByteArrayOutputStream().use {
      ObjectOutputStream(it).use {
        it.writeObject(obj)
      }
      // TODO: ObjectOutputStream.toByteArray() copies the bytes: not efficient
      val bytes = it.toByteArray()
      if (isKey && bytes.size > env.maxKeySize) {
        return hash(bytes)
      }
      // TODO: this copies bytes again: not efficient
      val buffer = ByteBuffer.allocateDirect(bytes.size)
      buffer.put(bytes).flip()
      return buffer
    }
  }

  private fun <T> deserialize(buffer: ByteBuffer): T? {
    ByteBufferBackedInputStream(buffer).use {
      ObjectInputStream(it).use {
        try {
          @Suppress("UNCHECKED_CAST")
          return it.readObject() as T
        } catch (e: ClassNotFoundException) {
          return null
        } catch (e: ObjectStreamException) {
          return null
        } catch (e: IOException) {
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


  override fun toString(): String {
    return "LMDBBuildStore"
  }
}

class ByteBufferBackedInputStream(private val buf: ByteBuffer) : InputStream() {
  @Throws(IOException::class)
  override fun read(): Int {
    if (!buf.hasRemaining()) {
      return -1
    }
    return buf.get().toInt() and 0xFF
  }

  @Throws(IOException::class)
  override fun read(bytes: ByteArray, offset: Int, length: Int): Int {
    if (!buf.hasRemaining()) {
      return -1
    }

    val minLength = Math.min(length, buf.remaining())
    buf.get(bytes, offset, minLength)
    return minLength
  }
}