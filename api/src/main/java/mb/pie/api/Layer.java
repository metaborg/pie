package mb.pie.api;

import java.io.Serializable;

/**
 * Internal layer for intercepting parts of task execution, used for validation.
 */
public interface Layer {
    void requireTopDownStart(TaskKey key, Serializable input);

    void requireTopDownEnd(TaskKey key);

    void validatePreWrite(TaskKey key, TaskData data, StoreReadTxn txn);

    void validatePostWrite(TaskKey key, TaskData data, StoreReadTxn txn);
}
