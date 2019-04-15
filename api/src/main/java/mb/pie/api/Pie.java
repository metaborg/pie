package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * PIE entry-point.
 */
public interface Pie extends AutoCloseable {
    /**
     * Creates a new session for incrementally executing tasks. Within a session, the same task is never executed more
     * than once. For sound incrementality, a new session must be started after external changes (such as file changes)
     * have occurred.
     */
    PieSession newSession();

    /**
     * Creates a new session for incrementally executing tasks, where the task definitions are replaced by
     * {@code taskDefs}. Within a session, the same task is never executed more than once. For sound incrementality, a
     * new session must be started after external changes (such as file changes) have occurred.
     */
    PieSession newSession(TaskDefs taskDefs);


    /**
     * Checks whether task with {@code key} has been executed at least once.
     */
    boolean hasBeenExecuted(TaskKey key);


    /**
     * Sets {@code observer} as the observer for outputs of {@code task}. Whenever {@code task} is required, its
     * up-to-date output will be observed by the {@code observer}. The {@link Task#key()} of {@code task} will be used
     * to identify which observer to call when a task is required.
     */
    <O extends @Nullable Serializable> void setObserver(Task<O> task, Consumer<O> observer);

    /**
     * Sets {@code observer} as the observer for outputs of task with {@code key}. Whenever task with {@code key} is
     * required, its up-to-date output will be observed by the {@code observer}. The output argument passed to the
     * {@code observer} function must be casted to the correct type.
     */
    void setObserver(TaskKey key, Consumer<@Nullable Serializable> observer);

    /**
     * Removes the observer for outputs of {@code task}. The {@link Task#key()} of {@code task} will be used to identify
     * which observer to remove.
     */
    void removeObserver(Task<?> task);

    /**
     * Removes the observer for outputs of task with {@code key}.
     */
    void removeObserver(TaskKey key);

    /**
     * Removes all (drops) observers.
     */
    void dropObservers();


    /**
     * Removes all data (drops) from the store.
     */
    void dropStore();
}
