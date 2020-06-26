package mb.pie.runtime;

import mb.pie.api.Callbacks;
import mb.pie.api.TaskKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class MapCallbacks implements Callbacks {
    protected final ConcurrentHashMap<TaskKey, Consumer<Serializable>> map = new ConcurrentHashMap<>();

    @Override
    public @Nullable Consumer<@Nullable Serializable> get(TaskKey key) {
        return map.get(key);
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
