package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.function.Consumer;

public interface Callbacks {
    @Nullable Consumer<@Nullable Serializable> get(TaskKey key);


    void set(TaskKey key, Consumer<@Nullable Serializable> function);

    default <O extends @Nullable Serializable> void set(Task<O> task, Consumer<O> function) {
        @SuppressWarnings("unchecked") final Consumer<@Nullable Serializable> obs = (Consumer<@Nullable Serializable>)function;
        set(task.key(), obs);
    }


    void remove(TaskKey key);

    default void remove(Task<?> task) {
        remove(task.key());
    }


    void clear();
}
