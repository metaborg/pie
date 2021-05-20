package mb.pie.runtime.store;

import mb.pie.api.serde.DeserializeRuntimeException;
import mb.pie.api.serde.Serde;
import mb.pie.api.Store;
import mb.pie.api.StoreReadTxn;
import mb.pie.api.StoreWriteTxn;
import mb.resource.ReadableResource;
import mb.resource.WritableResource;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.function.Supplier;

public class SerializingStore<S extends Store & Serializable> implements Store, Serializable {
    private final Serde serde;
    private final WritableResource resource;
    private final S store;
    private final boolean serializeOnSync;


    private SerializingStore(Serde serde, WritableResource resource, S store, boolean serializeOnSync) {
        this.serde = serde;
        this.resource = resource;
        this.store = store;
        this.serializeOnSync = serializeOnSync;
    }

    private SerializingStore(Serde serde, WritableResource resource, S store) {
        this(serde, resource, store, false);
    }

    public SerializingStore(Serde serde, WritableResource resource, Supplier<S> storeSupplier, Class<S> storeType, boolean serializeOnSync) {
        this(serde, resource, deserialize(serde, resource, storeSupplier, storeType), serializeOnSync);
    }

    public SerializingStore(Serde serde, WritableResource resource, Supplier<S> storeSupplier, Class<S> storeType) {
        this(serde, resource, deserialize(serde, resource, storeSupplier, storeType));
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
        try(final BufferedOutputStream bufferedOutputStream = resource.openWriteBuffered()) {
            serde.serialize(store, bufferedOutputStream);
            bufferedOutputStream.flush();
        } catch(IOException e) {
            throw new RuntimeException("Serializing store '" + store + "' failed unexpectedly", e);
        }
    }

    private static <S extends Store & Serializable> S deserialize(Serde serde, ReadableResource resource, Supplier<S> storeSupplier, Class<S> storeType) {
        try {
            if(!resource.exists()) {
                return storeSupplier.get();
            }
        } catch(IOException e) {
            return storeSupplier.get();
        }
        try(final BufferedInputStream bufferedInputStream = resource.openReadBuffered()) {
            return serde.deserialize(storeType, bufferedInputStream);
        } catch(DeserializeRuntimeException | IOException e) {
            // TODO: log error
            return storeSupplier.get();
        }
    }
}
