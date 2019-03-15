package mb.pie.store.lmdb;

import java.nio.ByteBuffer;

class BufferUtil {
    static ByteBuffer toBuffer(byte[] bytes) {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
        buffer.put(bytes).flip();
        return buffer;
    }

    static ByteBuffer copyBuffer(ByteBuffer byteBuffer) {
        final ByteBuffer readOnlyBuffer = byteBuffer.asReadOnlyBuffer();
        final ByteBuffer newBuffer = ByteBuffer.allocateDirect(readOnlyBuffer.capacity());
        readOnlyBuffer.rewind();
        newBuffer.put(readOnlyBuffer);
        newBuffer.flip();
        return newBuffer;
    }

    static ByteBuffer emptyBuffer() {
        return ByteBuffer.allocateDirect(0);
    }
}
