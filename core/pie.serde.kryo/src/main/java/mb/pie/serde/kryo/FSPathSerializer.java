package mb.pie.serde.kryo;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.Serializer;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import mb.resource.fs.FSPath;

import java.net.URI;

public class FSPathSerializer extends Serializer<FSPath> {
    public FSPathSerializer() {
        setImmutable(true);
    }

    @Override public void write(Kryo kryo, Output output, FSPath fsPath) {
        kryo.writeObject(output, fsPath.getURI());
    }

    @Override public FSPath read(Kryo kryo, Input input, Class<? extends FSPath> aClass) {
        final URI uri = kryo.readObject(input, URI.class);
        System.out.println(uri);
        return new FSPath(uri);
    }
}
