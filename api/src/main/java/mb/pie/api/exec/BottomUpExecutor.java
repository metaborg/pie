package mb.pie.api.exec;

import mb.pie.api.*;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Executor using a bottom-up build algorithm and observers for pushing new observed outputs.
 */
public interface BottomUpExecutor {
    /**
     * Make up-to-date all tasks affected by [changes to given resources][changedResources]. Changed outputs of tasks are observed by
     * observers.
     */
    void requireBottomUp(Set<ResourceKey> changedResources) throws ExecException;

    /**
     * Make up-to-date all tasks affected by [changes to given resources][changedResources]. Changed outputs of tasks are observed by
     * observers. Uses given [cancel] requester to check for cancellation.
     */
    void requireBottomUp(Set<ResourceKey> changedResources, Cancelled cancel) throws ExecException, InterruptedException;

    /**
     * Requires given [task] in a top-down fashion, returning its result.
     */
    <I extends Serializable, O extends @Nullable Serializable> @Nullable O requireTopDown(Task<I, O> task) throws ExecException;

    /**
     * Requires given [task] in a top-down fashion, with given [cancel] requester, returning its result.
     */
    <I extends Serializable, O extends @Nullable Serializable> @Nullable O requireTopDown(Task<I, O> task, Cancelled cancel) throws ExecException, InterruptedException;

    /**
     * Checks whether given task has been required at least once.
     */
    boolean hasBeenRequired(TaskKey key);

    /**
     * Sets [observer] as the observer for outputs of task [key], using given [key] which can be used to remove (unsubscribe from) the observer.
     */
    void setObserver(TaskKey key, Consumer<@Nullable Serializable> observer);

    /**
     * Removes the observer with given [key].
     */
    void removeObserver(TaskKey key);

    /**
     * Removes all (drops) observers.
     */
    void dropObservers();
}
