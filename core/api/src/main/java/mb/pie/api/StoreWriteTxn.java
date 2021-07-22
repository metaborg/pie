package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Collection;

/**
 * Storage read/write transaction. Must be closed after use.
 */
public interface StoreWriteTxn extends StoreReadTxn {
    /**
     * Sets the input of task [key] to [input].
     */
    void setInput(TaskKey key, Serializable input);

    /**
     * Sets the output of task [key] to [output].
     */
    void setOutput(TaskKey key, @Nullable Serializable output);

    /**
     * Sets the observability status to {@code observability} of task for {@code key}.
     */
    void setTaskObservability(TaskKey key, Observability observability);

    /**
     * Sets the task require dependencies of task [key] to [taskRequires].
     */
    void setTaskRequires(TaskKey key, Collection<TaskRequireDep> taskRequires);

    /**
     * Sets the resource require dependencies of task [key] to [resourceRequires].
     */
    void setResourceRequires(TaskKey key, Collection<ResourceRequireDep> resourceRequires);

    /**
     * Sets the resource provide dependencies of task [key] to [resourceProvides].
     */
    void setResourceProvides(TaskKey key, Collection<ResourceProvideDep> resourceProvides);


    /**
     * Sets the output and dependencies for task [key] to [data].
     */
    void setData(TaskKey key, TaskData data);

    /**
     * Deletes the data of task for {@code key}.
     *
     * @param key Key of task to delete data for.
     * @return deleted task data, or null if no data was deleted.
     */
    @Nullable TaskData deleteData(TaskKey key);


    void addDeferredTask(TaskKey key);

    void removeDeferredTask(TaskKey key);


    /**
     * Removes all data from (drops) the store.
     */
    void drop();
}
