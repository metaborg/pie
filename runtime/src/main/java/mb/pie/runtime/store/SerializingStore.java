package mb.pie.runtime.store;

import mb.pie.api.Store;
import mb.pie.api.StoreReadTxn;
import mb.pie.api.StoreWriteTxn;
import mb.resource.ReadableResource;
import mb.resource.WritableResource;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.function.Supplier;

public class SerializingStore<S extends Store & Serializable> implements Store, Serializable {
    private final WritableResource resource;
    private final S store;
    private final boolean serializeOnSync;


    public SerializingStore(WritableResource resource, S store, boolean serializeOnSync) {
        this.resource = resource;
        this.store = store;
        this.serializeOnSync = serializeOnSync;
    }

    public SerializingStore(WritableResource resource, S store) {
        this(resource, store, false);
    }

    public SerializingStore(WritableResource resource, Supplier<S> storeSupplier, boolean serializeOnSync) {
        this(resource, deserialize(resource, storeSupplier));
    }

    public SerializingStore(WritableResource resource, Supplier<S> storeSupplier) {
        this(resource, deserialize(resource, storeSupplier));
    }


    @Override public StoreReadTxn readTxn() {
        return store.readTxn();
    }

    @Override public StoreWriteTxn writeTxn() {
        return store.writeTxn();
    }

    @Override public void sync() throws IOException {
        store.sync();
        if(serializeOnSync) {
            serialize();
        }
    }

    @Override public void close() throws IOException {
        store.close();
        serialize();
    }


    private void serialize() throws IOException {
        try(
            final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(resource.openWrite());
            final ObjectOutputStream objectOutputStream = new ObjectOutputStream(bufferedOutputStream)
        ) {
            objectOutputStream.writeObject(store);
            objectOutputStream.flush();
        }
    }

    private static <S extends Store & Serializable> S deserialize(ReadableResource resource, Supplier<S> storeSupplier) {
        try {
            if(!resource.exists()) {
                return storeSupplier.get();
            }
        } catch(IOException e) {
            return storeSupplier.get();
        }

        try(
            final BufferedInputStream bufferedInputStream = new BufferedInputStream(resource.openRead());
            final ObjectInputStream objectInputStream = new ObjectInputStream(bufferedInputStream)
        ) {
            @SuppressWarnings("unchecked") final S deserialized = (S)objectInputStream.readObject();
            return deserialized;
        } catch(IOException | ClassNotFoundException e) {
            return storeSupplier.get();
        }
    }
}
