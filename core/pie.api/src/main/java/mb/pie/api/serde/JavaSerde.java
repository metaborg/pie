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
    private final @Nullable ClassLoader defaultClassLoader;


    public JavaSerde(@Nullable ClassLoader defaultClassLoader) {
        this.defaultClassLoader = defaultClassLoader;
    }

    public JavaSerde() {
        this(null);
    }


    @Override
    public <T> void serialize(T obj, OutputStream outputStream) {
        serializeTypeAndObject(obj, outputStream);
    }

    @Override
    public <T> T deserialize(Class<T> type, InputStream inputStream, @Nullable ClassLoader classLoader) {
        final @Nullable Object deserialized = deserializeObjectOfUnknownType(inputStream, getClassLoader(type, classLoader));
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
    public <T> @Nullable T deserializeNullable(Class<T> type, InputStream inputStream, @Nullable ClassLoader classLoader) {
        final @Nullable Object deserialized = deserializeObjectOfUnknownType(inputStream, getClassLoader(type, classLoader));
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
    public @Nullable Object deserializeObjectOfUnknownType(InputStream inputStream, @Nullable ClassLoader classLoader) {
        try(final ClassLoaderObjectInputStream objectInputStream = new ClassLoaderObjectInputStream(getClassLoader(classLoader), inputStream)) {
            return objectInputStream.readObject();
        } catch(IOException | ClassNotFoundException e) {
            throw new DeserializeRuntimeException(e);
        }
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
