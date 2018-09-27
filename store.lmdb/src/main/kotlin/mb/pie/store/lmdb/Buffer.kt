package mb.pie.store.lmdb

import java.nio.ByteBuffer


typealias Buf = ByteBuffer

@Suppress("NOTHING_TO_INLINE")
inline fun ByteArray.toBuffer(): Buf {
  val buffer = Buf.allocateDirect(size)
  buffer.put(this).flip()
  return buffer
}

@Suppress("NOTHING_TO_INLINE")
inline fun Buf.copyBuffer(): Buf {
  val readOnlyBuffer = asReadOnlyBuffer()
  val newBuffer = Buf.allocateDirect(readOnlyBuffer.capacity())
  readOnlyBuffer.rewind()
  newBuffer.put(readOnlyBuffer)
  newBuffer.flip()
  return newBuffer
}

@Suppress("NOTHING_TO_INLINE")
inline fun emptyBuffer(): Buf {
  return Buf.allocateDirect(0)
}
