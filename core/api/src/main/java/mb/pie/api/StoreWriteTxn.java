package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

/**
 * Storage read/write transaction. Must be closed after use.
 */
public interface StoreWriteTxn extends StoreReadTxn {
    /**
     * Resets {@code task}, removing its output, observability, outgoing task require dependencies, and resource
     * dependencies.
     */
    void resetTask(Task<?> task);

    /**
     * Adds a task require from task with key {@code caller} to task with key {@code callee}.
     */
    void addTaskRequire(TaskKey caller, TaskKey callee);

    /**
     * Adds task require dependency {@code dep} from task with key {@code caller}.
     */
    void addTaskRequireDep(TaskKey caller, TaskRequireDep dep);

    /**
     * Adds resource require dependency {@code dep} from task with key {@code requiree}.
     */
    void addResourceRequireDep(TaskKey requiree, ResourceRequireDep dep);

    /**
     * Adds resource provide dependency {@code dep} from task with key {@code provider}.
     */
    void addResourceProvideDep(TaskKey provider, ResourceProvideDep dep);

    /**
     * Sets the output of task with {@code key} to {@code output}.
     */
    void setOutput(TaskKey key, @Nullable Serializable output);

    /**
     * Sets the observability status of task with {@code key} to {@code observability}.
     */
    void setTaskObservability(TaskKey key, Observability observability);


    /**
     * Deletes the data of task for {@code key}.
     *
     * @param key Key of task to delete data for.
     * @return deleted task data, or null if no data was deleted.
     */
    @Nullable TaskData deleteData(TaskKey key);


    /**
     * Adds task with {@code} key to the deferred tasks.
     */
    void addDeferredTask(TaskKey key);

    /**
     * Removes task with {@code} key from the deferred tasks.
     */
    void removeDeferredTask(TaskKey key);


    /**
     * Removes all data from (drops) the store.
     */
    void drop();
}
