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
  fun setProduces(app: UBuildApp, res: UBuildRes)
  fun produces(app: UBuildApp): UBuildRes?

  fun setGeneratedBy(path: CPath, res: UBuildRes)
  fun generatedBy(path: CPath): UBuildRes?

  fun setRequiredBy(path: CPath, res: UBuildRes)
  fun requiredBy(path: CPath): UBuildRes?

  fun reset()
}

class InMemoryStore : Store {
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
}

class LMDBStore(envDir: File, numReaders : Int = 8) : Store {
  val env: Env<ByteBuffer>
  val produces: Dbi<ByteBuffer>
  val generatedBy: Dbi<ByteBuffer>
  val requiredBy: Dbi<ByteBuffer>


  init {
    envDir.mkdirs()
    env = Env.create().setMaxDbs(1024 * 1024).setMaxDbs(3).setMaxReaders(numReaders).open(envDir)
    produces = env.openDbi("produces", DbiFlags.MDB_CREATE)
    generatedBy = env.openDbi("generatedBy", DbiFlags.MDB_CREATE)
    requiredBy = env.openDbi("requiredBy", DbiFlags.MDB_CREATE)
  }

  override fun close() {
    env.close()
  }


  override fun setProduces(app: UBuildApp, res: UBuildRes) {
    val k = serialize(app)
    val v = serialize(res)
    env.txnWrite().use {
      produces.put(it, k, v)
      it.commit()
    }
  }

  override fun produces(app: UBuildApp): UBuildRes? {
    env.txnRead().use {
      val k = serialize(app)
      val v = produces.get(it, k)
      when (v) {
        null -> return null
        else -> return deserialize(v)
      }
    }
  }


  override fun setGeneratedBy(path: CPath, res: UBuildRes) {
    val k = serialize(path)
    val v = serialize(res)
    env.txnWrite().use {
      generatedBy.put(it, k, v)
      it.commit()
    }
  }

  override fun generatedBy(path: CPath): UBuildRes? {
    env.txnRead().use {
      val k = serialize(path)
      val v = generatedBy.get(it, k)
      when (v) {
        null -> return null
        else -> return deserialize(v)
      }
    }
  }


  override fun setRequiredBy(path: CPath, res: UBuildRes) {
    val k = serialize(path)
    val v = serialize(res)
    env.txnWrite().use {
      requiredBy.put(it, k, v)
      it.commit()
    }
  }

  override fun requiredBy(path: CPath): UBuildRes? {
    env.txnRead().use {
      val k = serialize(path)
      val v = requiredBy.get(it, k)
      when (v) {
        null -> return null
        else -> return deserialize(v)
      }
    }
  }


  override fun reset() {
    env.txnWrite().use {
      produces.drop(it)
      generatedBy.drop(it)
      requiredBy.drop(it)
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