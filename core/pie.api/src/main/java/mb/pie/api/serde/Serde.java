package mb.pie.api.serde;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * Serialization and deserialization. All serialization methods may throw {@link SerializeRuntimeException}, and all
 * deserialization methods may throw {@link DeserializeRuntimeException}. Both runtime exceptions always have a cause.
 * Implementations may require additional properties from objects passed to this interface. For example, Java
 * serialization requires that objects implement {@link Serializable}.
 */
public interface Serde {
    /**
     * Serializes given {@code obj} into {@code outputStream}. To deserialize, use the {@link #deserialize}, {@link
     * #deserializeFromBytes}, or {@link #deserializeFromByteBuffer} methods.
     */
    <T> void serialize(T obj, OutputStream outputStream);

    default <T> byte[] serializeToBytes(T obj) {
        try(final EfficientByteArrayOutputStream outputStream = new EfficientByteArrayOutputStream()) {
            serialize(obj, outputStream);
            return outputStream.toByteArray();
        } catch(IOException e) {
            throw new SerializeRuntimeException(e);
        }
    }

    default <T> ByteBuffer serializeToByteBuffer(T obj) {
        try(final EfficientByteArrayOutputStream outputStream = new EfficientByteArrayOutputStream()) {
            serialize(obj, outputStream);
            return outputStream.toByteBuffer();
        } catch(IOException e) {
            throw new SerializeRuntimeException(e);
        }
    }


    /**
     * Deserializes an object of {@code type} from {@code inputStream} using given {@code classLoader}.
     *
     * @param classLoader {@link ClassLoader} to deserialize the object with, or {@code null} to use an
     *                    implementation-defined classloader (typically the classloader of {@code type}).
     */
    <T> T deserialize(Class<T> type, InputStream inputStream, @Nullable ClassLoader classLoader);

    default <T> T deserialize(Class<T> type, InputStream inputStream) {
        return deserialize(type, inputStream, null);
    }

    default <T> T deserializeFromBytes(Class<T> type, byte[] bytes, @Nullable ClassLoader classLoader) {
        try(final ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            return deserialize(type, inputStream, classLoader);
        } catch(IOException e) {
            throw new DeserializeRuntimeException(e);
        }
    }

    default <T> T deserializeFromBytes(Class<T> type, byte[] bytes) {
        return deserializeFromBytes(type, bytes, null);
    }

    default <T> T deserializeFromByteBuffer(Class<T> type, ByteBuffer byteBuffer, @Nullable ClassLoader classLoader) {
        try(final ByteBufferBackedInputStream inputStream = new ByteBufferBackedInputStream(byteBuffer)) {
            return deserialize(type, inputStream, classLoader);
        } catch(IOException e) {
            throw new DeserializeRuntimeException(e);
        }
    }

    default <T> T deserializeFromByteBuffer(Class<T> type, ByteBuffer byteBuffer) {
        return deserializeFromByteBuffer(type, byteBuffer, null);
    }


    /**
     * Serializes given {@code obj} or {@code null} into {@code outputStream}. To deserialize, use the {@link
     * #deserializeNullable}, {@link #deserializeNullableFromBytes}, or {@link #deserializeNullableFromByteBuffer}
     * methods.
     */
    <T> void serializeNullable(@Nullable T obj, Class<T> type, OutputStream outputStream);

    default <T> byte[] serializeNullableToBytes(@Nullable T obj, Class<T> type) {
        try(final EfficientByteArrayOutputStream outputStream = new EfficientByteArrayOutputStream()) {
            serializeNullable(obj, type, outputStream);
            return outputStream.toByteArray();
        } catch(IOException e) {
            throw new SerializeRuntimeException(e);
        }
    }

    default <T> ByteBuffer serializeNullableToByteBuffer(@Nullable T obj, Class<T> type) {
        try(final EfficientByteArrayOutputStream outputStream = new EfficientByteArrayOutputStream()) {
            serializeNullable(obj, type, outputStream);
            return outputStream.toByteBuffer();
        } catch(IOException e) {
            throw new SerializeRuntimeException(e);
        }
    }


    /**
     * Deserializes an object of {@code type} or {@code null} from {@code inputStream} using given {@code classLoader}.
     *
     * @param classLoader {@link ClassLoader} to deserialize the object with, or {@code null} to use an
     *                    implementation-defined classloader (typically the classloader of {@code type}).
     */
    <T> @Nullable T deserializeNullable(Class<T> type, InputStream inputStream, @Nullable ClassLoader classLoader);

    default <T> @Nullable T deserializeNullable(Class<T> type, InputStream inputStream) {
        return deserializeNullable(type, inputStream, null);
    }

    default <T> @Nullable T deserializeNullableFromBytes(Class<T> type, byte[] bytes, @Nullable ClassLoader classLoader) {
        try(final ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            return deserializeNullable(type, inputStream, classLoader);
        } catch(IOException e) {
            throw new DeserializeRuntimeException(e);
        }
    }

    default <T> @Nullable T deserializeNullableFromBytes(Class<T> type, byte[] bytes) {
        return deserializeNullableFromBytes(type, bytes, null);
    }

    default <T> @Nullable T deserializeNullableFromByteBuffer(Class<T> type, ByteBuffer byteBuffer, @Nullable ClassLoader classLoader) {
        try(final ByteBufferBackedInputStream inputStream = new ByteBufferBackedInputStream(byteBuffer)) {
            return deserializeNullable(type, inputStream, classLoader);
        } catch(IOException e) {
            throw new DeserializeRuntimeException(e);
        }
    }

    default <T> @Nullable T deserializeNullableFromByteBuffer(Class<T> type, ByteBuffer byteBuffer) {
        return deserializeNullableFromByteBuffer(type, byteBuffer, null);
    }


    /**
     * Serializes given {@code obj} or {@code null} along with its type into {@code outputStream}, such that it can be
     * deserialized without statically knowing its type. To deserialize, use the {@link
     * #deserializeObjectOfUnknownType}, {@link #deserializeObjectOfUnknownTypeFromBytes}, or {@link
     * #deserializeObjectOfUnknownTypeFromByteBuffer} methods.
     */
    void serializeTypeAndObject(@Nullable Object obj, OutputStream outputStream);

    default byte[] serializeTypeAndObjectToBytes(@Nullable Object obj) {
        try(final EfficientByteArrayOutputStream outputStream = new EfficientByteArrayOutputStream()) {
            serializeTypeAndObject(obj, outputStream);
            outputStream.flush();
            return outputStream.toByteArray();
        } catch(IOException e) {
            throw new SerializeRuntimeException(e);
        }
    }

    default ByteBuffer serializeTypeAndObjectToByteBuffer(@Nullable Object obj) {
        try(final EfficientByteArrayOutputStream outputStream = new EfficientByteArrayOutputStream()) {
            serializeTypeAndObject(obj, outputStream);
            return outputStream.toByteBuffer();
        } catch(IOException e) {
            throw new SerializeRuntimeException(e);
        }
    }


    /**
     * Deserializes an object of unknown type (statically) or {@code null} from {@code inputStream} using given {@code
     * classLoader}.
     *
     * @param classLoader {@link ClassLoader} to deserialize the object with, or {@code null} to use an
     *                    implementation-defined classloader (typically the classloader of the class of this object).
     */
    @Nullable Object deserializeObjectOfUnknownType(InputStream inputStream, @Nullable ClassLoader classLoader);

    default @Nullable Object deserializeObjectOfUnknownType(InputStream inputStream) {
        return deserializeObjectOfUnknownType(inputStream, null);
    }

    default @Nullable Object deserializeObjectOfUnknownTypeFromBytes(byte[] bytes, @Nullable ClassLoader classLoader) {
        try(final ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            return deserializeObjectOfUnknownType(inputStream, classLoader);
        } catch(IOException e) {
            throw new DeserializeRuntimeException(e);
        }
    }

    default @Nullable Object deserializeObjectOfUnknownTypeFromBytes(byte[] bytes) {
        return deserializeObjectOfUnknownTypeFromBytes(bytes, null);
    }

    default @Nullable Object deserializeObjectOfUnknownTypeFromByteBuffer(ByteBuffer byteBuffer, @Nullable ClassLoader classLoader) {
        try(final ByteBufferBackedInputStream inputStream = new ByteBufferBackedInputStream(byteBuffer)) {
            return deserializeObjectOfUnknownType(inputStream, classLoader);
        } catch(IOException e) {
            throw new DeserializeRuntimeException(e);
        }
    }

    default @Nullable Object deserializeObjectOfUnknownTypeFromByteBuffer(ByteBuffer byteBuffer) {
        return deserializeObjectOfUnknownTypeFromByteBuffer(byteBuffer, null);
    }
}
