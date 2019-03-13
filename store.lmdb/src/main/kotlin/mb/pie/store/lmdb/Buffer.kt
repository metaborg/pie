package mb.pie.store.lmdb

import java.nio.ByteBuffer

object BufferUtil {
  public fun toBuffer(bytes: ByteArray): ByteBuffer {
    val buffer: ByteBuffer = ByteBuffer.allocateDirect(bytes.size);
    buffer.put(bytes).flip();
    return buffer;
  }

  public fun copyBuffer(byteBuffer: ByteBuffer): ByteBuffer {
    val readOnlyBuffer: ByteBuffer = byteBuffer.asReadOnlyBuffer();
    val newBuffer: ByteBuffer = ByteBuffer.allocateDirect(readOnlyBuffer.capacity());
    readOnlyBuffer.rewind();
    newBuffer.put(readOnlyBuffer);
    newBuffer.flip();
    return newBuffer;
  }

  public fun emptyBuffer(): ByteBuffer {
    return ByteBuffer.allocateDirect(0);
  }
}
