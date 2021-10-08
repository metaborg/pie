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
     * @return input for task with {@code key}, or {@code null} if no input is stored.
     */
    @Nullable Serializable getInput(TaskKey key);

    /**
     * @return internal object for task with {@code key}, or {@code null} if no internal object was stored or when
     * {@code null} was explicitly stored as the internal object
     */
    @Nullable Serializable getInternalObject(TaskKey key);

    /**
     * @return wrapper around output for task with {@code key}, or {@code null} if no output is stored.
     */
    @Nullable Output getOutput(TaskKey key);

    /**
     * @return observability of task with {@code key}.
     */
    Observability getTaskObservability(TaskKey key);


    /**
     * @return task require dependencies (calls) of task with key {@code caller}.
     */
    Collection<TaskRequireDep> getTaskRequireDeps(TaskKey caller);

    /**
     * @return required tasks of task with key {@code caller}.
     */
    Collection<TaskKey> getRequiredTasks(TaskKey caller);

    /**
     * @return callers of task with key {@code callee}.
     */
    Set<TaskKey> getCallersOf(TaskKey callee);


    /**
     * @return {@code true} if task with key {@code caller} requires task {@code callee} directly or transitively.
     * {@code false} otherwise.
     */
    boolean doesRequireTransitively(TaskKey caller, TaskKey callee);

    /**
     * @return {@code true} if task {@code caller} has dependency order before {@code callee}. {@code false} otherwise.
     */
    boolean hasDependencyOrderBefore(TaskKey caller, TaskKey callee);


    /**
     * @return resource require dependencies of task with key {@code requirer}.
     */
    Collection<ResourceRequireDep> getResourceRequireDeps(TaskKey requirer);

    /**
     * @return keys of tasks that require resource with key {@code requiree}.
     */
    Set<TaskKey> getRequirersOf(ResourceKey requiree);


    /**
     * @return resource provide dependencies of task with key {@code provider}.
     */
    Collection<ResourceProvideDep> getResourceProvideDeps(TaskKey provider);

    /**
     * @return task that provides resource with key {@code providee}, or {@code null} if no task provides it.
     */
    @Nullable TaskKey getProviderOf(ResourceKey providee);


    /**
     * @return output and dependencies for task [key], or {@code null} when no output was stored.
     */
    @Nullable TaskData getData(TaskKey key);


    /**
     * @return task keys for all tasks that are deferred during a bottom-up build.
     */
    Set<TaskKey> getDeferredTasks();


    /**
     * @return task keys for all tasks that have no callers.
     */
    Set<TaskKey> getTasksWithoutCallers();

    /**
     * @return number of source required resources for which there is no provider.
     */
    int getNumSourceFiles();
}
