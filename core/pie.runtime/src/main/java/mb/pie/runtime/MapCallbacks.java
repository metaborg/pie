package mb.pie.runtime;

import mb.pie.api.Callbacks;
import mb.pie.api.StoreReadTxn;
import mb.pie.api.TaskKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class MapCallbacks implements Callbacks {
    private final ConcurrentHashMap<TaskKey, Consumer<Serializable>> map = new ConcurrentHashMap<>();

    @Override
    public @Nullable Consumer<@Nullable Serializable> get(TaskKey key, StoreReadTxn txn) {
        final @Nullable Consumer<Serializable> mapCallback = map.get(key);
        final @Nullable Consumer<Serializable> storeCallback = txn.getCallback(key);
        if(mapCallback != null && storeCallback != null) {
            throw new RuntimeException("BUG: both a map callback '" + mapCallback + "' and a store callback '" + storeCallback + "' exists for task with key '" + key + "'");
        }
        if(mapCallback != null) {
            return mapCallback;
        }
        if(storeCallback != null) {
            return storeCallback;
        }
        return null;
    }

    @Override
    public void set(TaskKey key, Consumer<@Nullable Serializable> function) {
        map.put(key, function);
    }

    @Override
    public void remove(TaskKey key) {
        map.remove(key);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override public String toString() {
        return "MapCallbacks(" + map + ')';
    }
}
