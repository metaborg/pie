package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * PIE entry point.
 */
public interface Pie extends AutoCloseable {
    /**
     * Creates a new session for incrementally executing tasks. See {@link PieSession} for information
     * <p>
     * Within a session, the same task is never executed more than once. For sound incrementality, a new session must be
     * started after external changes have occurred. See {@link PieSession} for a list of external changes.
     *
     * @return A new session.
     * @see PieSession
     */
    PieSession newSession();

    /**
     * Creates a new session for incrementally executing tasks, with additional task definitions.
     * <p>
     * Within a session, the same task is never executed more than once. For sound incrementality, a new session must be
     * started after external changes have occurred. See {@link PieSession} for a list of external changes.
     *
     * @param addTaskDefs Additional {@link TaskDef task definitions} that are available to the created session.
     * @return A new session.
     * @see PieSession
     */
    PieSession newSession(TaskDefs addTaskDefs);


    /**
     * Checks whether task with given {@code key} has been executed at least once.
     *
     * @param key Key of task to check.
     */
    boolean hasBeenExecuted(TaskKey key);


    /**
     * Sets {@code observer} as the observer for outputs of {@code task}. Whenever {@code task} is required, its
     * up-to-date output will be observed by the {@code observer}.
     *
     * @param task     Task to observe. The {@link Task#key() key} of this task will be used to identify which observer
     *                 to call when a task is required.
     * @param observer Consumer (function) to call with up-to-date output when {@code task} is required.
     */
    <O extends @Nullable Serializable> void setObserver(Task<O> task, Consumer<O> observer);

    /**
     * Sets {@code observer} as the observer for outputs of task with {@code key}. Whenever task with {@code key} is
     * required, its up-to-date output will be observed by the {@code observer}.
     *
     * @param key      Key of task to observe.
     * @param observer Consumer (function) to call with up-to-date output when {@code task} is required. The output
     *                 argument passed to this observer must be casted to the correct type.
     */
    void setObserver(TaskKey key, Consumer<@Nullable Serializable> observer);

    /**
     * Removes the observer for outputs of {@code task}.
     *
     * @param task Task to remove observer for. The {@link Task#key()} of {@code task} will be used to identify which
     *             observer to remove.
     */
    void removeObserver(Task<?> task);

    /**
     * Removes the observer for outputs of task with {@code key}.
     *
     * @param key Key of task to remove observer for.
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
