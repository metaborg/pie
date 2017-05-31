package mb.ceres.internal

import mb.ceres.CPath
import mb.ceres.UBuildApp
import org.lmdbjava.Dbi
import org.lmdbjava.DbiFlags
import org.lmdbjava.Env
import java.io.*
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap

interface Store : AutoCloseable {
  operator fun set(app: UBuildApp, res: UBuildRes)
  operator fun set(path: CPath, res: UBuildRes)
  operator fun get(app: UBuildApp): UBuildRes?
  operator fun get(path: CPath): UBuildRes?
  fun reset()
}

class InMemoryStore : Store {
  val produces = ConcurrentHashMap<UBuildApp, UBuildRes>()
  val generates = ConcurrentHashMap<CPath, UBuildRes>()

  override fun set(app: UBuildApp, res: UBuildRes) {
    produces[app] = res
  }

  override fun set(path: CPath, res: UBuildRes) {
    generates[path] = res
  }

  override fun get(app: UBuildApp): UBuildRes? {
    return produces[app]
  }

  override fun get(path: CPath): UBuildRes? {
    return generates[path]
  }

  override fun reset() {
    produces.clear()
    generates.clear()
  }

  override fun close() {
    // No closing required
  }
}

class LMDBStore(envDir: File) : Store {
  val env: Env<ByteBuffer>
  val produces: Dbi<ByteBuffer>
  val generates: Dbi<ByteBuffer>

  init {
    envDir.mkdirs()
    env = Env.create().setMaxDbs(1024 * 1024).setMaxDbs(2).open(envDir)
    produces = env.openDbi("produces", DbiFlags.MDB_CREATE)
    generates = env.openDbi("generates", DbiFlags.MDB_CREATE)
  }

  override fun close() {
    env.close()
  }

  override fun set(app: UBuildApp, res: UBuildRes) {
    val k = serialize(app)
    val v = serialize(res)
    env.txnWrite().use {
      produces.put(it, k, v)
      it.commit()
    }
  }

  override fun set(path: CPath, res: UBuildRes) {
    val k = serialize(path)
    val v = serialize(res)
    env.txnWrite().use {
      generates.put(it, k, v)
      it.commit()
    }
  }

  override fun get(app: UBuildApp): UBuildRes? {
    env.txnRead().use {
      val k = serialize(app)
      val v = produces.get(it, k)
      when (v) {
        null -> return null
        else -> return deserialize(v)
      }
    }
  }

  override fun get(path: CPath): UBuildRes? {
    env.txnRead().use {
      val k = serialize(path)
      val v = generates.get(it, k)
      when (v) {
        null -> return null
        else -> return deserialize(v)
      }
    }
  }

  override fun reset() {
    env.txnWrite().use {
      produces.drop(it)
      generates.drop(it)
      it.commit()
    }
  }


  private fun <T> serialize(obj: T): ByteBuffer {
    ByteArrayOutputStream().use {
      ObjectOutputStream(it).use {
        it.writeObject(obj)
      }
      // TODO: ObjectOutputStream.toByteArray() copies the bytes: not efficient
      val bytes = it.toByteArray()
      // TODO: this copies bytes again: not efficient
      val buffer = ByteBuffer.allocateDirect(bytes.size)
      buffer.put(bytes).flip()
      return buffer
    }
  }

  private fun <T> deserialize(buffer: ByteBuffer): T {
    ByteBufferBackedInputStream(buffer).use {
      ObjectInputStream(it).use {
        @Suppress("UNCHECKED_CAST")
        return it.readObject() as T
      }
    }
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