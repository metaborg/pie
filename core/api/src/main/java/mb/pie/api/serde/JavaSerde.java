package mb.pie.api.serde;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * {@link Serde} implementation using Java serialization. All objects must implement {@link Serializable}.
 */
public class JavaSerde implements Serde {
    public final ClassLoader classLoader;


    public JavaSerde(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public JavaSerde() {
        this(JavaSerde.class.getClassLoader());
    }


    public JavaSerde withClassLoader(ClassLoader classLoader) {
        return new JavaSerde(classLoader);
    }


    @Override
    public <T> void serialize(T obj, OutputStream outputStream) {
        serializeTypeAndObject(obj, outputStream);
    }

    @Override
    public <T> T deserialize(Class<T> type, InputStream inputStream) {
        final @Nullable Object deserialized = deserializeTypeAndObject(inputStream);
        if(deserialized == null) {
            throw new DeserializeRuntimeException(new NullPointerException("Expected non-nullable deserialized object of type '" + type + "', but got null"));
        }
        return cast(type, deserialized);
    }


    @Override
    public <T> void serializeNullable(@Nullable T obj, Class<T> type, OutputStream outputStream) {
        serializeTypeAndObject(obj, outputStream);
    }

    @Override
    public <T> @Nullable T deserializeNullable(Class<T> type, InputStream inputStream) {
        final @Nullable Object deserialized = deserializeTypeAndObject(inputStream);
        if(deserialized == null) {
            return null;
        }
        return cast(type, deserialized);
    }

    @SuppressWarnings("unchecked")
    private <T> T cast(Class<T> type, Object deserialized) {
        final Class<?> deserializedType = deserialized.getClass();
        if(!type.equals(deserializedType)) {
            throw new DeserializeRuntimeException(new ClassCastException("Expected deserialized object of type '" + type + "', but got object of type '" + deserializedType + "'"));
        }
        return (T)deserialized;
    }


    @Override
    public void serializeTypeAndObject(@Nullable Object obj, OutputStream outputStream) {
        try(final ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
            objectOutputStream.writeObject(obj);
            objectOutputStream.flush();
        } catch(IOException e) {
            throw new SerializeRuntimeException(e);
        }
    }

    @Override
    public @Nullable Object deserializeTypeAndObject(InputStream inputStream) {
        try(final ClassLoaderObjectInputStream objectInputStream = new ClassLoaderObjectInputStream(classLoader, inputStream)) {
            return objectInputStream.readObject();
        } catch(IOException | ClassNotFoundException e) {
            throw new DeserializeRuntimeException(e);
        }
    }
}
