package mb.pie.store.lmdb;

import mb.log.api.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

class SerializeUtil {
    static <T extends @Nullable Serializable> byte[] serialize(T obj) {
        try(
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            final ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)
        ) {
            objectOutputStream.writeObject(obj);
            objectOutputStream.flush();
            outputStream.flush();
            // OPTO: copies bytes, not efficient
            return outputStream.toByteArray();
        } catch(IOException e) {
            // TODO: should this exception be checked?
            throw new UncheckedIOException(e);
        }
    }

    static byte[] hash(byte[] bytes) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-1");
            return digest.digest(bytes);
        } catch(NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    static <T extends @Nullable Serializable> SerializedAndHashed serializeAndHash(T obj) {
        final byte[] serialized = serialize(obj);
        final byte[] hashed = hash(serialized);
        return new SerializedAndHashed(serialized, hashed);
    }


    static <T extends @Nullable Serializable> ByteBuffer serializeHashedToBuffer(T obj) {
        return BufferUtil.toBuffer(SerializeUtil.hash(SerializeUtil.serialize(obj)));
    }

    public static <T extends @Nullable Serializable> ByteBuffer serializeToBuffer(T obj) {
        return BufferUtil.toBuffer(SerializeUtil.serialize(obj));
    }


    static <T extends @Nullable Serializable> Deserialized<T> deserialize(ByteBuffer byteBuffer, Logger logger) {
        try(
            final ByteBufferBackedInputStream bufferInputStream = new ByteBufferBackedInputStream(byteBuffer);
            final ObjectInputStream objectInputStream = new ObjectInputStream(bufferInputStream)
        ) {
            @SuppressWarnings("unchecked") final T deserialized = (T)objectInputStream.readObject();
            return new Deserialized<>(deserialized);
        } catch(ClassNotFoundException | IOException e) {
            logger.error("Deserialization failed", e);
            return new Deserialized<>();
        }
    }
}

