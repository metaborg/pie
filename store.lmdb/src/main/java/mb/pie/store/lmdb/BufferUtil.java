package mb.pie.store.lmdb;

import java.nio.ByteBuffer;

public class BufferUtil {
    public static ByteBuffer toBuffer(byte[] bytes) {
        final ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
        buffer.put(bytes).flip();
        return buffer;
    }

    public static ByteBuffer copyBuffer(ByteBuffer byteBuffer) {
        final ByteBuffer readOnlyBuffer = byteBuffer.asReadOnlyBuffer();
        final ByteBuffer newBuffer = ByteBuffer.allocateDirect(readOnlyBuffer.capacity());
        readOnlyBuffer.rewind();
        newBuffer.put(readOnlyBuffer);
        newBuffer.flip();
        return newBuffer;
    }

    public static ByteBuffer emptyBuffer() {
        return ByteBuffer.allocateDirect(0);
    }
}
