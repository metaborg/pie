package mb.pie.api;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * PIE entry point. All methods are thread-safe and reentrant by locking. A child {@link Pie} uses the same lock as its
 * parent. In other words, every tree of {@link Pie} objects uses the same lock.
 */
public interface Pie extends AutoCloseable {
    /**
     * Creates a new session for (incrementally) executing tasks.
     *
     * Only one session may exist per {@link Pie} object tree. Calling this method while another session exists will
     * result in this method waiting until that session is {@link MixedSession#close() closed} through locking.
     *
     * Within a session, the same task is never executed more than once. For sound incrementality, a new session must be
     * started after external changes have occurred. See {@link MixedSession} for a list of external changes.
     *
     * @return A new session.
     * @see MixedSession
     */
    MixedSession newSession();

    /**
     * Tries to create a new session for (incrementally) executing tasks, succeeding only if no other session exists for
     * this {@link Pie} object tree.
     *
     * Only one session may exist per {@link Pie} object tree. Calling this method while another session exists will
     * result in this method returning {@code Optional.empty()}.
     *
     * Within a session, the same task is never executed more than once. For sound incrementality, a new session must be
     * started after external changes have occurred. See {@link MixedSession} for a list of external changes.
     *
     * @return A new session if no other session exists, or empty if another session exists.
     * @see MixedSession
     */
    Optional<MixedSession> tryNewSession();


    /**
     * Checks whether {@code task} has been executed at least once.
     *
     * @param task Task to check. The {@link Task#key() key} of this task will be used to check.
     * @return True if task was executed at least once, false otherwise.
     * @deprecated Use {@link Session#hasBeenExecuted(Task)}.
     */
    @Deprecated default boolean hasBeenExecuted(Task<?> task) {
        return hasBeenExecuted(task.key());
    }

    /**
     * Checks whether task with given {@code key} has been executed at least once.
     *
     * @param key Key of task to check.
     * @return True if task was executed at least once, false otherwise.
     * @deprecated Use {@link Session#hasBeenExecuted(Task)}.
     */
    @Deprecated boolean hasBeenExecuted(TaskKey key);


    /**
     * Removes all data from the store.
     * @deprecated Use {@link Session#dropStore()}.
     */
    @Deprecated void dropStore();


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
