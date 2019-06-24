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
     * Checks whether {@code task} has been executed at least once.
     *
     * @param task Task to check. The {@link Task#key() key} of this task will be used to check.
     * @return True if task was executed at least once, false otherwise.
     */
    boolean hasBeenExecuted(Task<?> task);

    /**
     * Checks whether task with given {@code key} has been executed at least once.
     *
     * @param key Key of task to check.
     * @return True if task was executed at least once, false otherwise.
     */
    boolean hasBeenExecuted(TaskKey key);


    /**
     * Checks whether {@code task} is observed. A task is observed when it is marked as observed by the user with {@link
     * PieSession#setObserved(Task, boolean)}, or when a task is (transitively) required by an observed task.
     *
     * @param task Task to check. The {@link Task#key() key} of this task will be used to check.
     * @return True if task is observed, false otherwise.
     */
    boolean isObserved(Task<?> task);

    /**
     * Checks whether task with given {@code key} is observed. A task is observed when it is marked as observed by the
     * user with {@link PieSession#setObserved(Task, boolean)}, or when a task is (transitively) required by an observed
     * task.
     *
     * @param key Key of task to check.
     * @return True if task is observed, false otherwise.
     */
    boolean isObserved(TaskKey key);


    /**
     * Sets {@code function} as the callback for outputs of {@code task}. Whenever {@code task} is required, its
     * up-to-date output will be passed as an argument to the {@code function}.
     *
     * @param task     Task to set the callback for. The {@link Task#key() key} of this task will be used to identify
     *                 which callback function to call when a task is required.
     * @param function Function to call with up-to-date output as argument when {@code task} is required.
     */
    <O extends @Nullable Serializable> void setCallback(Task<O> task, Consumer<O> function);

    /**
     * Sets {@code function} as the callback for outputs of tasks with {@code key}. Whenever task with {@code key} is
     * required, its up-to-date output will be passed as an argument to the {@code function}.
     *
     * @param key      Key of task to set callback for.
     * @param function Function to call with up-to-date output as argument when task is required.
     */
    void setCallback(TaskKey key, Consumer<@Nullable Serializable> function);

    /**
     * Removes the callback function for outputs of {@code task}.
     *
     * @param task Task to remove the callback for. The {@link Task#key()} of {@code task} will be used to identify
     *             which callback function to remove.
     */
    void removeCallback(Task<?> task);

    /**
     * Removes the callback function for outputs of task with {@code key}.
     *
     * @param key Key of task to remove callback function for.
     */
    void removeCallback(TaskKey key);

    /**
     * Removes all (drops) callback functions.
     */
    void dropCallbacks();


    /**
     * Removes all data (drops) from the store.
     */
    void dropStore();
}
