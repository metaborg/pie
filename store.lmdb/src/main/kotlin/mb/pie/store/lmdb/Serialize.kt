package mb.pie.store.lmdb

import mb.pie.api.Logger
import java.io.*
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.*


@Suppress("NOTHING_TO_INLINE")
inline fun <T : Serializable?> T.serialize(): ByteArray {
  ByteArrayOutputStream().use { outputStream ->
    ObjectOutputStream(outputStream).use { objectOutputStream ->
      objectOutputStream.writeObject(this)
      objectOutputStream.flush()
    }
    outputStream.flush()
    // OPTO: copies bytes, not efficient
    return outputStream.toByteArray()
  }
}

@Suppress("NOTHING_TO_INLINE")
inline fun ByteArray.hash(): ByteArray {
  val digest = MessageDigest.getInstance("SHA-1")
  return digest.digest(this)
}

@Suppress("NOTHING_TO_INLINE")
inline fun <T : Serializable?> T.serializeAndHash(): SerializedAndHashed {
  val serialized = this.serialize()
  val hashed = serialized.hash()
  return SerializedAndHashed(serialized, hashed)
}

data class SerializedAndHashed(val serialized: ByteArray, val hashed: ByteArray) {
  override fun equals(other: Any?): Boolean {
    if(this === other) return true
    if(javaClass != other?.javaClass) return false

    other as SerializedAndHashed

    if(!Arrays.equals(serialized, other.serialized)) return false
    if(!Arrays.equals(hashed, other.hashed)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = Arrays.hashCode(serialized)
    result = 31 * result + Arrays.hashCode(hashed)
    return result
  }
}


fun <T : Serializable?> Buf.deserialize(logger: Logger): Deserialized<T> {
  ByteBufferBackedInputStream(this).use { bufferInputStream ->
    ObjectInputStream(bufferInputStream).use { objectInputStream ->
      return try {
        @Suppress("UNCHECKED_CAST")
        val deserialized = objectInputStream.readObject() as T
        Deserialized(deserialized)
      } catch(e: ClassNotFoundException) {
        logger.error("Deserialization failed", e)
        Deserialized<T>()
      } catch(e: IOException) {
        logger.error("Deserialization failed", e)
        Deserialized<T>()
      }
    }
  }
}

data class Deserialized<out R : Serializable?>(val deserialized: R, val failed: Boolean) {
  constructor(deserialized: R) : this(deserialized, false)
  @Suppress("UNCHECKED_CAST")
  constructor() : this(null as R, true)
}

@Suppress("NOTHING_TO_INLINE")
inline fun <R : Serializable?> Deserialized<R>?.orElse(default: R): R {
  return if(this == null || this.failed) {
    default
  } else {
    this.deserialized
  }
}

inline fun <R : Serializable?, RR> Deserialized<R>?.mapOrElse(default: RR, func: (R) -> RR): RR {
  return if(this == null || this.failed) {
    default
  } else {
    func(this.deserialized)
  }
}

private class ByteBufferBackedInputStream(private val buf: ByteBuffer) : InputStream() {
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


