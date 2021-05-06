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
    private final ClassLoader classLoaderFallback;
    private final @Nullable ClassLoader classLoaderOverride;


    private JavaSerde(ClassLoader classLoaderFallback, @Nullable ClassLoader classLoaderOverride) {
        this.classLoaderOverride = classLoaderOverride;
        this.classLoaderFallback = classLoaderFallback;
    }

    public JavaSerde() {
        this(JavaSerde.class.getClassLoader(), null);
    }


    public static JavaSerde createWithClassLoaderOverride(ClassLoader classLoaderOverride) {
        return new JavaSerde(JavaSerde.class.getClassLoader(), classLoaderOverride);
    }

    public static JavaSerde createWithClassLoaderFallback(ClassLoader classLoaderFallback) {
        return new JavaSerde(classLoaderFallback, null);
    }


    public JavaSerde withClassLoaderOverride(@Nullable ClassLoader classLoaderOverride) {
        return new JavaSerde(this.classLoaderFallback, classLoaderOverride);
    }

    public JavaSerde withClassLoaderFallback(ClassLoader classLoaderFallback) {
        return new JavaSerde(classLoaderFallback, this.classLoaderOverride);
    }


    @Override
    public <T> void serialize(T obj, OutputStream outputStream) {
        serializeTypeAndObject(obj, outputStream);
    }

    @Override
    public <T> T deserialize(Class<T> type, InputStream inputStream) {
        final @Nullable Object deserialized = deserializeTypeAndObject(getClassLoader(type), inputStream);
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
        final @Nullable Object deserialized = deserializeTypeAndObject(getClassLoader(type), inputStream);
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
    public @Nullable Object deserializeTypeAndObject(@Nullable ClassLoader classLoader, InputStream inputStream) {
        try(final ClassLoaderObjectInputStream objectInputStream = new ClassLoaderObjectInputStream(getClassLoader(classLoader), inputStream)) {
            return objectInputStream.readObject();
        } catch(IOException | ClassNotFoundException e) {
            throw new DeserializeRuntimeException(e);
        }
    }


    private ClassLoader getClassLoader(Class<?> type) {
        if(classLoaderOverride != null) return classLoaderOverride;
        return getClassLoader(type.getClassLoader());
    }

    private ClassLoader getClassLoader(@Nullable ClassLoader classLoader) {
        if(classLoaderOverride != null) return classLoaderOverride;
        if(classLoader != null) return classLoader;
        return this.classLoaderFallback;
    }
}
