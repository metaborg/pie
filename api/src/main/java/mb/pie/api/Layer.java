package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

/**
 * Internal layer for intercepting parts of task execution, used for validation.
 */
public interface Layer {
    void requireTopDownStart(TaskKey key, Serializable input);

    void requireTopDownEnd(TaskKey key);

    <I extends Serializable, @Nullable O extends Serializable> void validatePreWrite(TaskKey key, TaskData<I, O> data, StoreReadTxn txn);

    <I extends Serializable, @Nullable O extends Serializable> void validatePostWrite(TaskKey key, TaskData<I, O> data, StoreReadTxn txn);
}
