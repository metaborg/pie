package mb.pie.serde.kryo;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.ByteBufferInput;
import com.esotericsoftware.kryo.kryo5.io.ByteBufferOutput;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.esotericsoftware.kryo.kryo5.objenesis.strategy.StdInstantiatorStrategy;
import com.esotericsoftware.kryo.kryo5.serializers.ExternalizableSerializer;
import com.esotericsoftware.kryo.kryo5.serializers.JavaSerializer;
import com.esotericsoftware.kryo.kryo5.util.DefaultInstantiatorStrategy;
import mb.pie.api.serde.Serde;
import mb.resource.fs.FSPath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Externalizable;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URI;
import java.nio.ByteBuffer;

public class KryoSerde implements Serde {
    private final Kryo kryo;
    private final Output sharedStreamOutput;
    private final Input sharedStreamInput;
    private final Output sharedByteArrayOutput;
    private final Input sharedByteArrayInput;
    private final int byteBufferInitialSize;
    private final ByteBufferInput sharedByteBufferInput;

    public KryoSerde(Kryo kryo, int sharedBufferSize, int byteBufferInitialSize) {
        this.kryo = registerSerializers(kryo);
        this.sharedStreamOutput = new Output(sharedBufferSize, sharedBufferSize);
        this.sharedStreamInput = new Input(sharedBufferSize);
        this.sharedByteArrayOutput = new Output(sharedBufferSize, -1);
        this.byteBufferInitialSize = byteBufferInitialSize;
        this.sharedByteArrayInput = new Input();
        this.sharedByteBufferInput = new ByteBufferInput();
    }

    public KryoSerde(ClassLoader classLoader) {
        this(createDefaultKryo(classLoader), 4096, 512);
    }

    public KryoSerde() {
        this(KryoSerde.class.getClassLoader());
    }

    private static Kryo createDefaultKryo(ClassLoader classLoader) {
        final Kryo kryo = new Kryo();
        kryo.setClassLoader(classLoader);
        kryo.setReferences(true);

        // Configure Kryo to serialize objects with Java serialization as fallback, but this does not provide good performance.
        kryo.setRegistrationRequired(false);
        kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
        //kryo.addDefaultSerializer(Externalizable.class, ExternalizableSerializer.class);
        //kryo.addDefaultSerializer(Serializable.class, JavaSerializer.class);

        return registerSerializers(kryo);
    }

    private static Kryo registerSerializers(Kryo kryo) {
        kryo.register(URI.class, new URISerializer());
        kryo.register(FSPath.class, new FSPathSerializer());
        return kryo;
    }


    public KryoSerde withClassLoader(ClassLoader classLoader) {
        return new KryoSerde(classLoader);
    }


    @Override
    public <T> void serialize(T obj, OutputStream outputStream) {
        sharedStreamOutput.setOutputStream(outputStream);
        try {
            kryo.writeObject(sharedStreamOutput, obj);
        } finally {
            sharedStreamOutput.flush();
        }
    }

    @Override
    public <T> byte[] serializeToBytes(T obj) {
        try {
            kryo.writeObject(sharedByteArrayOutput, obj);
            return sharedByteArrayOutput.toBytes();
        } finally {
            sharedByteArrayOutput.reset();
        }
    }

    @Override
    public <T> ByteBuffer serializeToByteBuffer(T obj) {
        try(final ByteBufferOutput output = new ByteBufferOutput(byteBufferInitialSize, -1)) {
            kryo.writeObject(output, obj);
            return output.getByteBuffer();
        }
    }


    @Override
    public <T> T deserialize(Class<T> type, InputStream inputStream) {
        sharedStreamInput.setInputStream(inputStream);
        return kryo.readObject(sharedStreamInput, type);
    }

    @Override
    public <T> T deserializeFromBytes(Class<T> type, byte[] bytes) {
        sharedByteArrayInput.setBuffer(bytes);
        return kryo.readObject(sharedByteArrayInput, type);
    }

    @Override
    public <T> T deserializeFromByteBuffer(Class<T> type, ByteBuffer bytes) {
        sharedByteBufferInput.setBuffer(bytes);
        return kryo.readObject(sharedByteBufferInput, type);
    }


    @Override
    public <T> void serializeNullable(@Nullable T obj, Class<T> type, OutputStream outputStream) {
        sharedStreamOutput.setOutputStream(outputStream);
        try {
            kryo.writeObjectOrNull(sharedStreamOutput, obj, type);
        } finally {
            sharedStreamOutput.flush();
        }
    }

    @Override
    public <T> byte[] serializeNullableToBytes(@Nullable T obj, Class<T> type) {
        try {
            kryo.writeObjectOrNull(sharedByteArrayOutput, obj, type);
            return sharedByteArrayOutput.toBytes();
        } finally {
            sharedByteArrayOutput.reset();
        }
    }

    @Override
    public <T> ByteBuffer serializeNullableToByteBuffer(@Nullable T obj, Class<T> type) {
        try(final ByteBufferOutput output = new ByteBufferOutput(byteBufferInitialSize, -1)) {
            kryo.writeObjectOrNull(output, obj, type);
            return output.getByteBuffer();
        }
    }


    @Override
    public <T> @Nullable T deserializeNullable(Class<T> type, InputStream inputStream) {
        sharedStreamInput.setInputStream(inputStream);
        return kryo.readObjectOrNull(sharedStreamInput, type);
    }

    @Override
    public <T> @Nullable T deserializeNullableFromBytes(Class<T> type, byte[] bytes) {
        sharedByteArrayInput.setBuffer(bytes);
        return kryo.readObjectOrNull(sharedByteArrayInput, type);
    }

    @Override
    public <T> @Nullable T deserializeNullableFromByteBuffer(Class<T> type, ByteBuffer bytes) {
        sharedByteBufferInput.setBuffer(bytes);
        return kryo.readObjectOrNull(sharedByteBufferInput, type);
    }


    @Override
    public void serializeTypeAndObject(@Nullable Object obj, OutputStream outputStream) {
        sharedStreamOutput.setOutputStream(outputStream);
        try {
            kryo.writeClassAndObject(sharedStreamOutput, obj);
        } finally {
            sharedStreamOutput.flush();
        }
    }

    @Override
    public byte[] serializeTypeAndObjectToBytes(@Nullable Object obj) {
        try {
            kryo.writeClassAndObject(sharedByteArrayOutput, obj);
            return sharedByteArrayOutput.toBytes();
        } finally {
            sharedByteArrayOutput.reset();
        }
    }

    @Override
    public ByteBuffer serializeTypeAndObjectToByteBuffer(@Nullable Object obj) {
        try(final ByteBufferOutput output = new ByteBufferOutput(byteBufferInitialSize, -1)) {
            kryo.writeClassAndObject(output, obj);
            return output.getByteBuffer();
        }
    }


    @Override
    public @Nullable Object deserializeTypeAndObject(InputStream inputStream) {
        sharedStreamInput.setInputStream(inputStream);
        return kryo.readClassAndObject(sharedStreamInput);
    }

    @Override
    public @Nullable Object deserializeTypeAndObjectFromBytes(byte[] bytes) {
        sharedByteArrayInput.setBuffer(bytes);
        return kryo.readClassAndObject(sharedByteArrayInput);
    }

    @Override
    public @Nullable Object deserializeTypeAndObjectFromByteBuffer(ByteBuffer bytes) {
        sharedByteBufferInput.setBuffer(bytes);
        return kryo.readClassAndObject(sharedByteBufferInput);
    }
}
