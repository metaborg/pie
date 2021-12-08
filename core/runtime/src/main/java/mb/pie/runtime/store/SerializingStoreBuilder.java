package mb.pie.runtime.store;

import mb.common.result.ThrowingSupplier;
import mb.log.api.LoggerFactory;
import mb.pie.api.Store;
import mb.pie.api.serde.Serde;
import mb.resource.WritableResource;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SerializingStoreBuilder<S extends Store & Serializable> {
    private final Serde serde;
    private final Supplier<S> storeSupplier;
    private final Class<S> storeType;

    private @MonotonicNonNull ThrowingSupplier<Optional<BufferedInputStream>, IOException> inputStreamSupplier;
    private @Nullable ClassLoader deserializeClassLoader;
    private @MonotonicNonNull ThrowingSupplier<BufferedOutputStream, IOException> outputStreamSupplier;

    private Consumer<Exception> serializeFailHandler = e -> {
        throw new RuntimeException("Serializing store failed", e);
    };
    private Consumer<Exception> deserializeFailHandler = e -> {
        throw new RuntimeException("Deserializing store failed", e);
    };

    private boolean serializeOnSync = false;


    public SerializingStoreBuilder(Serde serde, Supplier<S> storeSupplier, Class<S> storeType) {
        this.serde = serde;
        this.storeSupplier = storeSupplier;
        this.storeType = storeType;
    }


    public static SerializingStoreBuilder<InMemoryStore> ofInMemoryStore(Serde serde) {
        return new SerializingStoreBuilder<>(serde, InMemoryStore::new, InMemoryStore.class);
    }


    public SerializingStoreBuilder<S> withInputStreamSupplier(ThrowingSupplier<Optional<BufferedInputStream>, IOException> inputStreamSupplier) {
        this.inputStreamSupplier = inputStreamSupplier;
        return this;
    }

    public SerializingStoreBuilder<S> withDeserializeClassLoader(@Nullable ClassLoader deserializeClassLoader) {
        this.deserializeClassLoader = deserializeClassLoader;
        return this;
    }

    public SerializingStoreBuilder<S> withOutputStreamSupplier(ThrowingSupplier<BufferedOutputStream, IOException> outputStreamSupplier) {
        this.outputStreamSupplier = outputStreamSupplier;
        return this;
    }

    public SerializingStoreBuilder<S> withResourceStorage(WritableResource resource) {
        this.inputStreamSupplier = () -> {
            if(!resource.exists()) return Optional.empty();
            else return Optional.of(resource.openReadBuffered());
        };
        this.outputStreamSupplier = resource::openWriteBuffered;
        return this;
    }

    public SerializingStoreBuilder<S> withInMemoryBuffer(SerializingStoreInMemoryBuffer buffer) {
        this.inputStreamSupplier = buffer::getInputStream;
        this.outputStreamSupplier = buffer::getOutputStream;
        return this;
    }


    public SerializingStoreBuilder<S> withSerializeFailHandler(Consumer<Exception> serializeFailHandler) {
        this.serializeFailHandler = serializeFailHandler;
        return this;
    }

    public SerializingStoreBuilder<S> withDeserializeFailHandler(Consumer<Exception> deserializeFailHandler) {
        this.deserializeFailHandler = deserializeFailHandler;
        return this;
    }

    public SerializingStoreBuilder<S> withLoggingDeserializeFailHandler(LoggerFactory loggerFactory) {
        this.deserializeFailHandler = e -> loggerFactory.create(SerializingStore.class).error("Deserializing store failed", e);
        return this;
    }


    public SerializingStoreBuilder<S> withSerializeOnSync(boolean serializeOnSync) {
        this.serializeOnSync = serializeOnSync;
        return this;
    }


    public SerializingStore<S> build() {
        if(outputStreamSupplier == null)
            throw new IllegalStateException("Cannot build SerializingStore; outputStreamSupplier has not been set");
        if(inputStreamSupplier == null)
            throw new IllegalStateException("Cannot build SerializingStore; inputStreamSupplier has not been set");

        return new SerializingStore<>(
            serde,
            inputStreamSupplier,
            deserializeClassLoader,
            outputStreamSupplier,
            storeSupplier,
            storeType,
            serializeFailHandler,
            deserializeFailHandler,
            serializeOnSync
        );
    }
}
