package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

/**
 * Internal layer for intercepting parts of task execution, used for validation.
 */
public interface Layer {
    void requireTopDownStart(TaskKey key, Serializable input);

    void requireTopDownEnd(TaskKey key);

    void validateVisited(TaskKey key, Task<?> task, TaskData data);

    void validateTaskRequire(TaskKey caller, TaskKey callee, StoreReadTxn txn);

    void validateResourceRequireDep(TaskKey requirer, ResourceRequireDep dep, StoreReadTxn txn);

    void validateResourceProvideDep(TaskKey provider, ResourceProvideDep dep, StoreReadTxn txn);

    void validateTaskOutput(TaskKey key, @Nullable Serializable output, StoreReadTxn txn);
}
