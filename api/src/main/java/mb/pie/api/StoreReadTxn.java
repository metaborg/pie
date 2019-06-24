package mb.pie.api;

import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.List;
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
    List<TaskRequireDep> taskRequires(TaskKey key);

    /**
     * @return callers of task [key].
     */
    Set<TaskKey> callersOf(TaskKey key);


    /**
     * @return resource require dependencies of task [key].
     */
    List<ResourceRequireDep> resourceRequires(TaskKey key);

    /**
     * @return tasks that require resource [key].
     */
    Set<TaskKey> requireesOf(ResourceKey key);


    /**
     * @return resource provide dependencies of task [key].
     */
    List<ResourceProvideDep> resourceProvides(TaskKey key);

    /**
     * @return task that provides resource [key], or `null` if no task provides it.
     */
    @Nullable TaskKey providerOf(ResourceKey key);


    /**
     * @return output and dependencies for task [key], or `null` when no output was stored.
     */
    @Nullable TaskData data(TaskKey key);


    /**
     * @return number of source required resources for which there is no provider.
     */
    int numSourceFiles();
}
