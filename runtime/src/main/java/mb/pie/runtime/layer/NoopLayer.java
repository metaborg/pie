package mb.pie.runtime.layer;

import mb.pie.api.Layer;
import mb.pie.api.StoreReadTxn;
import mb.pie.api.TaskData;
import mb.pie.api.TaskKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

/**
 * A build layer that does nothing. For debugging or benchmarking purposes only.
 * DO NOT USE in production, as it disables checking for inconsistencies in the build.
 */
public class NoopLayer implements Layer {
    @Override public void requireTopDownStart(TaskKey key, Serializable input) {

    }

    @Override public void requireTopDownEnd(TaskKey key) {

    }

    @Override
    public <I extends Serializable, O extends @Nullable Serializable> void validatePreWrite(TaskKey key, TaskData<I, O> data, StoreReadTxn txn) {

    }

    @Override
    public <I extends Serializable, O extends @Nullable Serializable> void validatePostWrite(TaskKey key, TaskData<I, O> data, StoreReadTxn txn) {

    }
}
