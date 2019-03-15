package mb.pie.store.lmdb;

import java.io.InputStream;
import java.nio.ByteBuffer;

class ByteBufferBackedInputStream extends InputStream {
    private final ByteBuffer buf;

    ByteBufferBackedInputStream(ByteBuffer buf) {
        super();
        this.buf = buf;
    }

    @Override public int read() {
        if(!buf.hasRemaining()) {
            return -1;
        }
        return ((int) buf.get()) & 0xFF;
    }

    @Override public int read(byte[] bytes, int offset, int length) {
        if(!buf.hasRemaining()) {
            return -1;
        }

        final int minLength = Math.min(length, buf.remaining());
        buf.get(bytes, offset, minLength);
        return minLength;
    }
}
