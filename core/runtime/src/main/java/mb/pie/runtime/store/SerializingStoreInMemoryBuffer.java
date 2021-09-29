package mb.pie.runtime.store;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

public class SerializingStoreInMemoryBuffer implements AutoCloseable {
    private byte[] buffer = new byte[0];


    @Override public void close() throws Exception {
        buffer = new byte[0];
    }


    Optional<BufferedInputStream> getInputStream() {
        if(buffer.length == 0) return Optional.empty();
        return Optional.of(new BufferedInputStream(new ByteArrayInputStream(buffer)));
    }

    BufferedOutputStream getOutputStream() {
        return new BufferedOutputStream(new StoringByteArrayOutputStream());
    }

    private class StoringByteArrayOutputStream extends ByteArrayOutputStream {
        @Override public void close() throws IOException {
            super.close();
            buffer = buf;
        }
    }
}
