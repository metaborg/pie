package mb.pie.serde.kryo;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;

import java.net.URI;

public class URISerializer extends Serializer<URI> {
    public URISerializer() {
        setImmutable(true);
    }

    @Override
    public void write(final Kryo kryo, final Output output, final URI uri) {
        output.writeString(uri.toString());
    }

    @Override
    public URI read(final Kryo kryo, final Input input, final Class<? extends URI> uriClass) {
        return URI.create(input.readString());
    }
}
