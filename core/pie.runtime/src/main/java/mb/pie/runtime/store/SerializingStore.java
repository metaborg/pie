package mb.pie.runtime.store;

import mb.common.result.ThrowingSupplier;
import mb.pie.api.Store;
import mb.pie.api.StoreReadTxn;
import mb.pie.api.StoreWriteTxn;
import mb.pie.api.serde.DeserializeRuntimeException;
import mb.pie.api.serde.Serde;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SerializingStore<S extends Store & Serializable> implements Store {
    private final Serde serde;
    private final ThrowingSupplier<BufferedOutputStream, IOException> outputStreamSupplier;
    private final S store;
    private final Consumer<Exception> serializeFailHandler;
    private final boolean serializeOnSync;


    private SerializingStore(
        Serde serde,
        ThrowingSupplier<BufferedOutputStream, IOException> outputStreamSupplier,
        S store,
        Consumer<Exception> serializeFailHandler,
        boolean serializeOnSync
    ) {
        this.serde = serde;
        this.outputStreamSupplier = outputStreamSupplier;
        this.store = store;
        this.serializeFailHandler = serializeFailHandler;
        this.serializeOnSync = serializeOnSync;
    }


    public SerializingStore(
        Serde serde,
        ThrowingSupplier<Optional<BufferedInputStream>, IOException> inputStreamSupplier,
        @Nullable ClassLoader deserializeClassLoader,
        ThrowingSupplier<BufferedOutputStream, IOException> outputStreamSupplier,
        Supplier<S> storeSupplier,
        Class<S> storeType,
        Consumer<Exception> serializeFailHandler,
        Consumer<Exception> deserializeFailHandler,
        boolean serializeOnSync
    ) {
        this(
            serde,
            outputStreamSupplier,
            deserialize(serde, inputStreamSupplier, deserializeClassLoader, storeSupplier, storeType, deserializeFailHandler),
            serializeFailHandler,
            serializeOnSync
        );
    }


    @Override public StoreReadTxn readTxn() {
        return store.readTxn();
    }

    @Override public StoreWriteTxn writeTxn() {
        return store.writeTxn();
    }

    @Override public void sync() {
        store.sync();
        if(serializeOnSync) {
            serialize();
        }
    }

    @Override public void close() {
        store.close();
        serialize();
    }


    private void serialize() {
        try(final BufferedOutputStream bufferedOutputStream = outputStreamSupplier.get()) {
            serde.serialize(store, bufferedOutputStream);
            bufferedOutputStream.flush();
        } catch(IOException e) {
            serializeFailHandler.accept(e);
        }
    }

    private static <S extends Store & Serializable> S deserialize(
        Serde serde,
        ThrowingSupplier<Optional<BufferedInputStream>, IOException> inputStreamSupplier,
        @Nullable ClassLoader classLoader,
        Supplier<S> storeSupplier,
        Class<S> storeType,
        Consumer<Exception> deserializeFailHandler
    ) {
        final Optional<BufferedInputStream> option;
        try {
            option = inputStreamSupplier.get();
            if(!option.isPresent()) {
                return storeSupplier.get();
            }
        } catch(IOException e) {
            deserializeFailHandler.accept(e);
            return storeSupplier.get();
        }

        try(final BufferedInputStream bufferedInputStream = option.get()) {
            return serde.deserialize(storeType, bufferedInputStream, classLoader);
        } catch(DeserializeRuntimeException | IOException e) {
            deserializeFailHandler.accept(e);
            return storeSupplier.get();
        }
    }
}
