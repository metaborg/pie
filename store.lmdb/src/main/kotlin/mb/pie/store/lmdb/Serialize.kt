package mb.pie.store.lmdb

import mb.pie.api.Logger
import java.io.*
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.*
import java.util.function.Function

object SerializeUtil {
  public fun <T : Serializable?> serialize(obj: T): ByteArray {
    ByteArrayOutputStream().use { outputStream ->
      ObjectOutputStream(outputStream).use { objectOutputStream ->
        objectOutputStream.writeObject(obj);
        objectOutputStream.flush();
      }
      outputStream.flush();
      // OPTO: copies bytes, not efficient
      return outputStream.toByteArray();
    }
  }

  public fun hash(bytes: ByteArray): ByteArray {
    val digest: MessageDigest = MessageDigest.getInstance("SHA-1");
    return digest.digest(bytes);
  }

  public fun <T : Serializable?> serializeAndHash(obj: T): SerializedAndHashed {
    val serialized: ByteArray = serialize(obj);
    val hashed: ByteArray = hash(serialized);
    return SerializedAndHashed(serialized,hashed);
  }

  public fun <T : Serializable?> deserialize(byteBuffer: ByteBuffer,logger: Logger): Deserialized<T> {
    ByteBufferBackedInputStream(byteBuffer).use { bufferInputStream ->
      ObjectInputStream(bufferInputStream).use { objectInputStream ->
        return try {
          val deserialized: T = objectInputStream.readObject() as T;
          Deserialized(deserialized);
        } catch(e: ClassNotFoundException) {
          logger.error("Deserialization failed",e);
          Deserialized<T>();
        } catch(e: IOException) {
          logger.error("Deserialization failed",e);
          Deserialized<T>();
        }
      }
    }
  }
}

public class SerializedAndHashed {
  public val serialized: ByteArray;
  public val hashed: ByteArray;

  public constructor(serialized: ByteArray,hashed: ByteArray) {
    this.serialized = serialized;
    this.hashed = hashed;
  }

  override fun equals(other: Any?): Boolean {
    if(this === other) return true;
    if(javaClass != other?.javaClass) return false;
    other as SerializedAndHashed;
    if(!Arrays.equals(serialized,other.serialized)) return false;
    if(!Arrays.equals(hashed,other.hashed)) return false;
    return true;
  }

  override fun hashCode(): Int {
    var result: Int = Arrays.hashCode(serialized);
    result = 31 * result + Arrays.hashCode(hashed);
    return result;
  }
}

public class Deserialized<out R : Serializable?> {
  public val deserialized: R;
  public val failed: Boolean;

  public constructor(deserialized: R,failed: Boolean) {
    this.deserialized = deserialized;
    this.failed = failed;
  }

  public constructor(deserialized: R) : this(deserialized,false)
  public constructor() : this(null as R,true)

  companion object {
    public fun <R : Serializable?> orElse(deserialized: Deserialized<R>?,default: R): R {
      if(deserialized == null || deserialized.failed) {
        return default
      } else {
        return deserialized.deserialized
      }
    }

    public fun <R : Serializable?,RR> mapOrElse(deserialized: Deserialized<R>?,default: RR,func: Function<R,RR>): RR {
      if(deserialized == null || deserialized.failed) {
        return default
      } else {
        return func.apply(deserialized.deserialized)
      }
    }
  }
}

private class ByteBufferBackedInputStream : InputStream {
  private val buf: ByteBuffer

  public constructor(buf: ByteBuffer) : super() {
    this.buf = buf;
  }

  @Throws(IOException::class)
  override fun read(): Int {
    if(!buf.hasRemaining()) {
      return -1;
    }
    return buf.get().toInt() and 0xFF;
  }

  @Throws(IOException::class)
  override fun read(bytes: ByteArray,offset: Int,length: Int): Int {
    if(!buf.hasRemaining()) {
      return -1;
    }

    val minLength: Int = Math.min(length,buf.remaining());
    buf.get(bytes,offset,minLength);
    return minLength;
  }
}
