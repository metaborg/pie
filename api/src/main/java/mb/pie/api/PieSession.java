package mb.pie.api;

import mb.pie.api.exec.Cancelled;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Set;

/**
 * A session for executing PIE tasks.
 */
public interface PieSession extends AutoCloseable {
    /**
     * Requires {@code task} in a top-down fashion, returning its output.
     */
    <O extends Serializable> O requireTopDown(Task<O> task) throws ExecException;

    /**
     * Requires {@code task} in a top-down fashion, using given {@code cancel} requester, returning its output.
     */
    <O extends @Nullable Serializable> O requireTopDown(Task<O> task, Cancelled cancel) throws ExecException, InterruptedException;


    /**
     * Make up-to-date all tasks affected by {@code changedResources}. Changed outputs of tasks are observed by observers.
     */
    void requireBottomUp(Set<ResourceKey> changedResources) throws ExecException;

    /**
     * Make up-to-date all tasks affected by {@code changedResources}, using given {@code cancel} requester. Changed
     * outputs of tasks are observed by observers.
     */
    void requireBottomUp(Set<ResourceKey> changedResources, Cancelled cancel) throws ExecException, InterruptedException;
}
