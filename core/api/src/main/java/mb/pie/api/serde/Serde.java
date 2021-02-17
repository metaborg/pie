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


    <T> T deserialize(Class<T> type, InputStream inputStream);

    default <T> T deserializeFromBytes(Class<T> type, byte[] bytes) {
        try(final ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            return deserialize(type, inputStream);
        } catch(IOException e) {
            throw new DeserializeRuntimeException(e);
        }
    }

    default <T> T deserializeFromByteBuffer(Class<T> type, ByteBuffer byteBuffer) {
        try(final ByteBufferBackedInputStream inputStream = new ByteBufferBackedInputStream(byteBuffer)) {
            return deserialize(type, inputStream);
        } catch(IOException e) {
            throw new DeserializeRuntimeException(e);
        }
    }


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


    <T> @Nullable T deserializeNullable(Class<T> type, InputStream inputStream);

    default <T> @Nullable T deserializeNullableFromBytes(Class<T> type, byte[] bytes) {
        try(final ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            return deserializeNullable(type, inputStream);
        } catch(IOException e) {
            throw new DeserializeRuntimeException(e);
        }
    }

    default <T> @Nullable T deserializeNullableFromByteBuffer(Class<T> type, ByteBuffer ByteBuffer) {
        try(final ByteBufferBackedInputStream inputStream = new ByteBufferBackedInputStream(ByteBuffer)) {
            return deserializeNullable(type, inputStream);
        } catch(IOException e) {
            throw new DeserializeRuntimeException(e);
        }
    }


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


    @Nullable Object deserializeTypeAndObject(@Nullable ClassLoader classLoader, InputStream inputStream);

    default @Nullable Object deserializeTypeAndObjectFromBytes(@Nullable ClassLoader classLoader, byte[] bytes) {
        try(final ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes)) {
            return deserializeTypeAndObject(classLoader, inputStream);
        } catch(IOException e) {
            throw new DeserializeRuntimeException(e);
        }
    }

    default @Nullable Object deserializeTypeAndObjectFromByteBuffer(@Nullable ClassLoader classLoader, ByteBuffer ByteBuffer) {
        try(final ByteBufferBackedInputStream inputStream = new ByteBufferBackedInputStream(ByteBuffer)) {
            return deserializeTypeAndObject(classLoader, inputStream);
        } catch(IOException e) {
            throw new DeserializeRuntimeException(e);
        }
    }
}
