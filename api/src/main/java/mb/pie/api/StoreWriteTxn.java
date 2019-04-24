package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.ArrayList;

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
     * Sets the task require dependencies of task [key] to [taskRequires].
     */
    void setTaskRequires(TaskKey key, ArrayList<TaskRequireDep> taskRequires);

    /**
     * Sets the resource require dependencies of task [key] to [resourceRequires].
     */
    void setResourceRequires(TaskKey key, ArrayList<ResourceRequireDep> resourceRequires);

    /**
     * Sets the resource provide dependencies of task [key] to [resourceProvides].
     */
    void setResourceProvides(TaskKey key, ArrayList<ResourceProvideDep> resourceProvides);

    /**
     * Sets the output and dependencies for task [key] to [data].
     */
    void setData(TaskKey key, TaskData data);

    /**
     * Removes all data from (drops) the store.
     */
    void drop();
}
