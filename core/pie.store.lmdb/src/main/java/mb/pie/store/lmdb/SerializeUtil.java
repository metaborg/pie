package mb.pie.store.lmdb;

import mb.log.api.Logger;
import mb.log.api.LoggerFactory;
import mb.pie.api.serde.DeserializeRuntimeException;
import mb.pie.api.serde.Serde;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class SerializeUtil {
    private final Serde serde;
    private final Logger logger;


    SerializeUtil(Serde serde, LoggerFactory loggerFactory) {
        this.serde = serde;
        this.logger = loggerFactory.create(SerializeUtil.class);
    }


    <T> byte[] serializeToBytes(T obj) {
        return serde.serializeToBytes(obj);
    }

    <T> byte[] serializeHashedToBytes(T obj) {
        final byte[] serialized = serializeToBytes(obj);
        return hash(serialized);
    }

    <T> ByteBuffer serialize(T obj) {
        return serde.serializeToByteBuffer(obj);
    }

    <T> ByteBuffer serializeHashed(T obj) {
        final byte[] serialized = serializeToBytes(obj);
        final byte[] hashed = hash(serialized);
        return BufferUtil.toBuffer(hashed);
    }

    <T> SerializedAndHashed serializeAndHash(T obj) {
        final byte[] serialized = serializeToBytes(obj);
        final byte[] hashed = hash(serialized);
        return new SerializedAndHashed(serialized, hashed);
    }

    <T> De<T> deserialize(Class<T> type, ByteBuffer byteBuffer) {
        try {
            final T deserialized = serde.deserializeFromByteBuffer(type, byteBuffer);
            return new De<>(deserialized);
        } catch(DeserializeRuntimeException e) {
            logger.error("Deserialization failed", e.getCause());
            return new De<>();
        }
    }


    <T> byte[] serializeNullableToBytes(@Nullable T obj, Class<T> type) {
        return serde.serializeNullableToBytes(obj, type);
    }

    <T> byte[] serializeNullableHashedToBytes(@Nullable T obj, Class<T> type) {
        final byte[] serialized = serializeNullableToBytes(obj, type);
        return hash(serialized);
    }

    <T> ByteBuffer serializeNullable(@Nullable T obj, Class<T> type) {
        return serde.serializeNullableToByteBuffer(obj, type);
    }

    <T> ByteBuffer serializeNullableHashed(@Nullable T obj, Class<T> type) {
        final byte[] serialized = serializeNullableToBytes(obj, type);
        final byte[] hashed = hash(serialized);
        return BufferUtil.toBuffer(hashed);
    }

    <T> De<@Nullable T> deserializeNullable(Class<T> type, ByteBuffer byteBuffer) {
        try {
            final @Nullable T deserialized = serde.deserializeNullableFromByteBuffer(type, byteBuffer);
            return new De<>(deserialized);
        } catch(DeserializeRuntimeException e) {
            logger.error("Deserialization failed", e.getCause());
            return new De<>();
        }
    }


    byte[] serializeObjectToBytes(@Nullable Object obj) {
        return serde.serializeTypeAndObjectToBytes(obj);
    }

    byte[] serializeObjectHashedToBytes(@Nullable Object obj) {
        final byte[] serialized = serializeObjectToBytes(obj);
        return hash(serialized);
    }

    ByteBuffer serializeObject(@Nullable Object obj) {
        return serde.serializeTypeAndObjectToByteBuffer(obj);
    }

    ByteBuffer serializeObjectHashed(@Nullable Object obj) {
        final byte[] serialized = serializeObjectToBytes(obj);
        final byte[] hashed = hash(serialized);
        return BufferUtil.toBuffer(hashed);
    }

    @SuppressWarnings("unchecked") <T> De<@Nullable T> deserializeObject(ByteBuffer byteBuffer) {
        try {
            // TODO: pass in the correct classloader for deserialization.
            final @Nullable Object deserialized = serde.deserializeObjectOfUnknownTypeFromByteBuffer(byteBuffer, getClass().getClassLoader());
            return new De<>((T)deserialized);
        } catch(DeserializeRuntimeException e) {
            logger.error("Deserialization failed", e.getCause());
            return new De<>();
        }
    }


    byte[] hash(byte[] bytes) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-1");
            return digest.digest(bytes);
        } catch(NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}

