package mb.pie.serde.kryo;

import mb.resource.fs.FSPath;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class KryoSerdeTest {
    final KryoSerde serde = new KryoSerde();

    final FSPath path = new FSPath("/path/to/");
    final FSPath appendedPath = path.appendSegment("file.txt");

    @Test public void serdeRoundTripTest() throws IOException {
        final byte[] bytes;
        try(final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            serde.serialize(path, outputStream);
            outputStream.flush();
            bytes = outputStream.toByteArray();
        }
        final FSPath deserializedPath;
        try(final ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            deserializedPath = serde.deserialize(path.getClass(), inputStream);
        }

        assertEquals(path, deserializedPath);
        final FSPath appendedDeserializedPath = deserializedPath.appendSegment("file.txt");
        assertEquals(appendedPath, appendedDeserializedPath);
    }

    @Test public void serdeBytesRoundTripTest() {
        final byte[] bytes = serde.serializeToBytes(path);
        final FSPath deserializedPath = serde.deserializeFromBytes(path.getClass(), bytes);

        assertEquals(path, deserializedPath);
        final FSPath appendedDeserializedPath = deserializedPath.appendSegment("file.txt");
        assertEquals(appendedPath, appendedDeserializedPath);
    }
}
