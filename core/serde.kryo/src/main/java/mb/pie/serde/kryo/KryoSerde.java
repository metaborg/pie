package mb.pie.serde.kryo;

import com.esotericsoftware.kryo.kryo5.Kryo;
import com.esotericsoftware.kryo.kryo5.io.ByteBufferInput;
import com.esotericsoftware.kryo.kryo5.io.ByteBufferOutput;
import com.esotericsoftware.kryo.kryo5.io.Input;
import com.esotericsoftware.kryo.kryo5.io.Output;
import com.esotericsoftware.kryo.kryo5.objenesis.strategy.StdInstantiatorStrategy;
import com.esotericsoftware.kryo.kryo5.util.DefaultInstantiatorStrategy;
import mb.pie.api.serde.Serde;
import mb.resource.fs.FSPath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.ByteBuffer;

public class KryoSerde implements Serde {
    private final @Nullable ClassLoader defaultClassLoader;
    private final Kryo kryo;
    private final Output sharedStreamOutput;
    private final Input sharedStreamInput;
    private final Output sharedByteArrayOutput;
    private final Input sharedByteArrayInput;
    private final int byteBufferInitialSize;
    private final ByteBufferInput sharedByteBufferInput;

    public KryoSerde(@Nullable ClassLoader defaultClassLoader, Kryo kryo, int sharedBufferSize, int byteBufferInitialSize) {
        this.defaultClassLoader = defaultClassLoader;
        this.kryo = registerSerializers(kryo);
        this.sharedStreamOutput = new Output(sharedBufferSize, sharedBufferSize);
        this.sharedStreamInput = new Input(sharedBufferSize);
        this.sharedByteArrayOutput = new Output(sharedBufferSize, -1);
        this.byteBufferInitialSize = byteBufferInitialSize;
        this.sharedByteArrayInput = new Input();
        this.sharedByteBufferInput = new ByteBufferInput();
    }

    public KryoSerde(@Nullable ClassLoader defaultClassLoader) {
        this(defaultClassLoader, createDefaultKryo(), 4096, 512);
    }

    public KryoSerde() {
        this(null);
    }

    private static Kryo createDefaultKryo() {
        final Kryo kryo = new Kryo();
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
    public <T> T deserialize(Class<T> type, InputStream inputStream, @Nullable ClassLoader classLoader) {
        sharedStreamInput.setInputStream(inputStream);
        kryo.setClassLoader(getClassLoader(type, classLoader));
        return kryo.readObject(sharedStreamInput, type);
    }

    @Override
    public <T> T deserializeFromBytes(Class<T> type, byte[] bytes, @Nullable ClassLoader classLoader) {
        sharedByteArrayInput.setBuffer(bytes);
        kryo.setClassLoader(getClassLoader(type, classLoader));
        return kryo.readObject(sharedByteArrayInput, type);
    }

    @Override
    public <T> T deserializeFromByteBuffer(Class<T> type, ByteBuffer bytes, @Nullable ClassLoader classLoader) {
        sharedByteBufferInput.setBuffer(bytes);
        kryo.setClassLoader(getClassLoader(type, classLoader));
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
    public <T> @Nullable T deserializeNullable(Class<T> type, InputStream inputStream, @Nullable ClassLoader classLoader) {
        sharedStreamInput.setInputStream(inputStream);
        kryo.setClassLoader(getClassLoader(type, classLoader));
        return kryo.readObjectOrNull(sharedStreamInput, type);
    }

    @Override
    public <T> @Nullable T deserializeNullableFromBytes(Class<T> type, byte[] bytes, @Nullable ClassLoader classLoader) {
        sharedByteArrayInput.setBuffer(bytes);
        kryo.setClassLoader(getClassLoader(type, classLoader));
        return kryo.readObjectOrNull(sharedByteArrayInput, type);
    }

    @Override
    public <T> @Nullable T deserializeNullableFromByteBuffer(Class<T> type, ByteBuffer bytes, @Nullable ClassLoader classLoader) {
        sharedByteBufferInput.setBuffer(bytes);
        kryo.setClassLoader(getClassLoader(type, classLoader));
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
    public @Nullable Object deserializeObjectOfUnknownType(InputStream inputStream, @Nullable ClassLoader classLoader) {
        sharedStreamInput.setInputStream(inputStream);
        kryo.setClassLoader(getClassLoader(classLoader));
        return kryo.readClassAndObject(sharedStreamInput);
    }

    @Override
    public @Nullable Object deserializeObjectOfUnknownTypeFromBytes(byte[] bytes, @Nullable ClassLoader classLoader) {
        sharedByteArrayInput.setBuffer(bytes);
        kryo.setClassLoader(getClassLoader(classLoader));
        return kryo.readClassAndObject(sharedByteArrayInput);
    }

    @Override
    public @Nullable Object deserializeObjectOfUnknownTypeFromByteBuffer(ByteBuffer bytes, @Nullable ClassLoader classLoader) {
        sharedByteBufferInput.setBuffer(bytes);
        kryo.setClassLoader(getClassLoader(classLoader));
        return kryo.readClassAndObject(sharedByteBufferInput);
    }


    private ClassLoader getClassLoader(Class<?> type, @Nullable ClassLoader classLoader) {
        if(classLoader != null) return classLoader;
        if(defaultClassLoader != null) return defaultClassLoader;
        return getClassLoader(type.getClassLoader());
    }

    private ClassLoader getClassLoader(@Nullable ClassLoader classLoader) {
        if(classLoader != null) return classLoader;
        if(defaultClassLoader != null) return defaultClassLoader;
        return getClass().getClassLoader();
    }
}
