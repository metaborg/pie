package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.function.Consumer;

public interface Callbacks {
    @Nullable Consumer<Serializable> get(TaskKey key);


    void set(TaskKey key, Consumer<Serializable> function);

    default <O extends Serializable> void set(Task<O> task, Consumer<O> function) {
        @SuppressWarnings("unchecked") final Consumer<Serializable> obs = (Consumer<Serializable>)function;
        set(task.key(), obs);
    }


    void remove(TaskKey key);

    default void remove(Task<?> task) {
        remove(task.key());
    }


    void clear();
}
