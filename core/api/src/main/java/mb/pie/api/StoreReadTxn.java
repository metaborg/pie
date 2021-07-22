package mb.pie.api;

import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

/**
 * Storage read transaction. Must be closed after use.
 */
public interface StoreReadTxn extends StoreTxn {
    /**
     * @return input for task with [key], or `null` if no input is stored.
     */
    @Nullable Serializable input(TaskKey key);

    /**
     * @return wrapper around output for [key], or `null` if no output is stored.
     */
    @Nullable Output output(TaskKey key);


    /**
     * @return observability status of task for {@code key}.
     */
    Observability taskObservability(TaskKey key);


    /**
     * @return task require dependencies (calls) of task [key].
     */
    Collection<TaskRequireDep> taskRequires(TaskKey key);

    /**
     * @return callers of task [key].
     */
    Set<TaskKey> callersOf(TaskKey key);

    /**
     * @return {@code true} if task {@code caller} requires task {@code callee} directly or transitively. {@code false}
     * otherwise.
     */
    boolean requiresTransitively(TaskKey caller, TaskKey callee);

    /**
     * @return {@code true} if task {@code caller} has dependency order before {@code callee}. {@code false}
     * otherwise.
     */
    boolean hasDependencyOrderBefore(TaskKey caller, TaskKey callee);


    /**
     * @return resource require dependencies of task [key].
     */
    Collection<ResourceRequireDep> resourceRequires(TaskKey key);

    /**
     * @return tasks that require resource [key].
     */
    Set<TaskKey> requireesOf(ResourceKey key);


    /**
     * @return resource provide dependencies of task [key].
     */
    Collection<ResourceProvideDep> resourceProvides(TaskKey key);

    /**
     * @return task that provides resource [key], or `null` if no task provides it.
     */
    @Nullable TaskKey providerOf(ResourceKey key);


    /**
     * @return output and dependencies for task [key], or `null` when no output was stored.
     */
    @Nullable TaskData data(TaskKey key);


    Set<TaskKey> deferredTasks();

    /**
     * @return task keys for all tasks that have no callers.
     */
    Set<TaskKey> tasksWithoutCallers();

    /**
     * @return number of source required resources for which there is no provider.
     */
    int numSourceFiles();
}
