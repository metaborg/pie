package mb.pie.runtime;

import mb.pie.api.Callbacks;
import mb.pie.api.TaskKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class CompositeCallbacks implements Callbacks {
    private final Callbacks childCallbacks;
    private final List<Callbacks> parentCallbacks;

    public CompositeCallbacks(Callbacks childCallbacks, Collection<Callbacks> parentCallbacks) {
        this.childCallbacks = childCallbacks;
        this.parentCallbacks = new ArrayList<>(parentCallbacks);
    }

    @Override
    public @Nullable Consumer<@Nullable Serializable> get(TaskKey key) {
        final @Nullable Consumer<@Nullable Serializable> callback = childCallbacks.get(key);
        if(callback != null) {
            return callback;
        }
        return parentCallbacks
            .stream()
            .map(callbacks -> callbacks.get(key))
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }

    @Override
    public void set(TaskKey key, Consumer<@Nullable Serializable> function) {
        childCallbacks.set(key, function);
    }

    @Override
    public void remove(TaskKey key) {
        childCallbacks.remove(key);
    }

    @Override
    public void clear() {
        childCallbacks.clear();
    }

    @Override public String toString() {
        return "CompositeCallbacks{child=" + childCallbacks + ", parent=" + parentCallbacks + '}';
    }
}
