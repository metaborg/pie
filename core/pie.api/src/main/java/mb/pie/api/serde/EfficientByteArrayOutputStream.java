package mb.pie.api.serde;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

class EfficientByteArrayOutputStream extends ByteArrayOutputStream {
    EfficientByteArrayOutputStream() {}

    EfficientByteArrayOutputStream(int size) {
        super(size);
    }

    public byte[] toByteArray() {
        if(buf.length == count) {
            return buf;
        }
        final byte[] bytes = new byte[count];
        System.arraycopy(buf, 0, bytes, 0, count);
        return bytes;
    }

    public ByteBuffer toByteBuffer() {
        final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(count);
        byteBuffer.put(buf, 0, count).flip();
        return byteBuffer;
    }
}
