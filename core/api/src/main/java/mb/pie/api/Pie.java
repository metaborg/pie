package mb.pie.api;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * PIE entry point.
 */
public interface Pie extends AutoCloseable {
    /**
     * Creates a new session for incrementally executing tasks.
     *
     * Within a session, the same task is never executed more than once. For sound incrementality, a new session must be
     * started after external changes have occurred. See {@link MixedSession} for a list of external changes.
     *
     * @return A new session.
     * @see MixedSession
     */
    MixedSession newSession();


    /**
     * Checks whether {@code task} has been executed at least once.
     *
     * @param task Task to check. The {@link Task#key() key} of this task will be used to check.
     * @return True if task was executed at least once, false otherwise.
     */
    default boolean hasBeenExecuted(Task<?> task) {
        return hasBeenExecuted(task.key());
    }

    /**
     * Checks whether task with given {@code key} has been executed at least once.
     *
     * @param key Key of task to check.
     * @return True if task was executed at least once, false otherwise.
     */
    boolean hasBeenExecuted(TaskKey key);


    /**
     * Checks whether {@code task} is observed, either explicitly (by requiring it with a top-down build) or implicitly
     * (when another observed task requires it).
     *
     * @param task Task to check. The {@link Task#key() key} of this task will be used to check.
     * @return True if task is observed, false otherwise.
     */
    default boolean isObserved(Task<?> task) {
        return isObserved(task.key());
    }

    /**
     * Checks whether task with given {@code key} is observed, either explicitly (by requiring it with a top-down build)
     * or implicitly (when another observed task requires it).
     *
     * @param key Key of task to check.
     * @return True if task is observed, false otherwise.
     */
    boolean isObserved(TaskKey key);


    /**
     * Checks whether {@code task} is explicitly observed (by requiring it with a top-down build).
     *
     * @param task Task to check. The {@link Task#key() key} of this task will be used to check.
     * @return True if task is explicitly observed, false otherwise.
     */
    default boolean isExplicitlyObserved(Task<?> task) {
        return isExplicitlyObserved(task.key());
    }

    /**
     * Checks whether task with given {@code key} is explicitly observed (by requiring it with a top-down build).
     *
     * @param key Key of task to check.
     * @return True if task is explicitly observed, false otherwise.
     */
    boolean isExplicitlyObserved(TaskKey key);


    /**
     * Sets the observability of {@code task} to {@link Observability#ExplicitObserved explicitly observed} if it is
     * {@link Observability#ImplicitObserved implicitly observed}. Does nothing if already {@link
     * Observability#ExplicitObserved explicitly observed}. Throws if {@link Observability#Unobserved unobserved}. Use
     * {@link Session#require} to explicitly observe an unobserved task.
     *
     * @param task Task to explicitly observe.
     * @throws IllegalArgumentException when {@code} task is not observed.
     */
    default void setImplicitToExplicitlyObserved(Task<?> task) {
        setImplicitToExplicitlyObserved(task.key());
    }

    /**
     * Sets the observability of task with given {@code key} to {@link Observability#ExplicitObserved explicitly
     * observed} if it is {@link Observability#ImplicitObserved implicitly observed}. Does nothing if already {@link
     * Observability#ExplicitObserved explicitly observed}. Throws if {@link Observability#Unobserved unobserved}. Use
     * {@link Session#require} to explicitly observe an unobserved task.
     *
     * @param key Key of task to explicitly observe.
     * @throws IllegalArgumentException when {@code} task is not observed.
     */
    void setImplicitToExplicitlyObserved(TaskKey key);


    /**
     * Sets {@code function} as the callback for outputs of {@code task}. Whenever {@code task} is required, its
     * up-to-date output will be passed as an argument to the {@code function}.
     *
     * @param task     Task to set the callback for. The {@link Task#key() key} of this task will be used to identify
     *                 which callback function to call when a task is required.
     * @param function Function to call with up-to-date output as argument when {@code task} is required. Consumed value
     *                 by the function may be {@code null} when a task returns {@code null}.
     */
    <O extends Serializable> void setCallback(Task<O> task, Consumer<O> function);

    /**
     * Sets {@code function} as the callback for outputs of tasks with {@code key}. Whenever task with {@code key} is
     * required, its up-to-date output will be passed as an argument to the {@code function}.
     *
     * @param key      Key of task to set callback for.
     * @param function Function to call with up-to-date output as argument when task is required. Consumed value by the
     *                 function may be {@code null} when a task returns {@code null}.
     */
    void setCallback(TaskKey key, Consumer<Serializable> function);

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
     * Removes all callback functions.
     */
    void dropCallbacks();


    /**
     * Removes all data from the store.
     */
    void dropStore();


    /**
     * Creates a {@link PieChildBuilder builder} for creating a child {@link Pie} instance, with this {@link Pie}
     * instance as its parent.
     *
     * @param ancestors {@link Pie} instances that will serve as ancestor for the child {@link Pie} instance.
     * @return {@link PieChildBuilder Builder} for creating a child {@link Pie} instance
     */
    PieChildBuilder createChildBuilder(Pie... ancestors);


    /**
     * Registers itself as an ancestor of the {@link Pie} instance that will be created by the {@code childBuilder}.
     *
     * @param childBuilder {@link PieChildBuilder child builder} to register {@code this} as ancestor for.
     */
    void addToChildBuilder(PieChildBuilder childBuilder);


    /**
     * Closes the PIE entry point, {@link Store#close() closing the storage}.
     *
     * @throws RuntimeException when closing fails.
     */
    void close();
}
