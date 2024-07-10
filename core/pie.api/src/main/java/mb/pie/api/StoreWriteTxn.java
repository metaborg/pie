package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

/**
 * Storage read/write transaction. Must be closed after use.
 */
public interface StoreWriteTxn extends StoreReadTxn {
    /**
     * Resets {@code task}, removing its output, observability, outgoing task require dependencies, and resource
     * dependencies. Sets the input of {@code task} to {@code task.input}. Does not remove its incoming task require
     * dependencies, nor its internal object.
     *
     * @return {@link TaskData} containing the previous input of the task, along with all the data that was removed, or
     * {@code null} if no data was stored for {@code task} (i.e., it did not exist).
     */
    @Nullable TaskData resetTask(Task<?> task);

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
     * Sets the internal object of task with {@code key} to {@code obj}.
     */
    void setInternalObject(TaskKey key, @Nullable Serializable obj);

    /**
     * Clears the internal object of task with {@code key}.
     */
    void clearInternalObject(TaskKey key);


    /**
     * Restores the data of task with {@code key} to {@code data}, or reset its data if {@code data} is {@code null}.
     *
     * @param key  Key of task to restore.
     * @param data Data to restore, or {@code null} if there is no previous data to restore.
     */
    void restoreData(TaskKey key, @Nullable TaskData data);

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
     * Sets the callback of task for {@code key} to {@code callback}.
     */
    void setCallback(TaskKey key, SerializableConsumer<Serializable> callback);

    /**
     * Removes the callback of task for {@code key}. Does nothing if it has no callback.
     */
    void removeCallback(TaskKey key);

    /**
     * Removes all callbacks.
     */
    void dropCallbacks();


    /**
     * Removes all data from (drops) the store.
     */
    void drop();
}
