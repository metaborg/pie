package mb.pie.runtime.layer;

import mb.pie.api.Layer;
import mb.pie.api.StoreReadTxn;
import mb.pie.api.TaskData;
import mb.pie.api.TaskKey;

import java.io.Serializable;

/**
 * A build layer that does nothing. For debugging or benchmarking purposes only.
 * DO NOT USE in production, as it disables checking for inconsistencies in the build.
 */
public class NoopLayer implements Layer {
    @Override public void requireTopDownStart(TaskKey key, Serializable input) {}

    @Override public void requireTopDownEnd(TaskKey key) {}

    @Override public void validatePreWrite(TaskKey key, TaskData data, StoreReadTxn txn) {}

    @Override public void validatePostWrite(TaskKey key, TaskData data, StoreReadTxn txn) {}
}
