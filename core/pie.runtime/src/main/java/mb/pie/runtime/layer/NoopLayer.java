package mb.pie.runtime.layer;

import mb.pie.api.Layer;
import mb.pie.api.ResourceProvideDep;
import mb.pie.api.ResourceRequireDep;
import mb.pie.api.StoreReadTxn;
import mb.pie.api.Task;
import mb.pie.api.TaskData;
import mb.pie.api.TaskKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

/**
 * A build layer that does nothing. For debugging or benchmarking purposes only. DO NOT USE in production, as it
 * disables checking for inconsistencies in the build.
 */
public class NoopLayer implements Layer {
    @Override
    public void requireTopDownStart(TaskKey key, Serializable input) {}

    @Override
    public void requireTopDownEnd(TaskKey key) {}

    @Override
    public void validateVisited(TaskKey key, Task<?> task, TaskData data) {}

    @Override
    public void validateTaskRequire(TaskKey caller, TaskKey callee, StoreReadTxn txn) {}

    @Override
    public void validateResourceRequireDep(TaskKey requirer, ResourceRequireDep dep, StoreReadTxn txn) {}

    @Override
    public void validateResourceProvideDep(TaskKey provider, ResourceProvideDep dep, StoreReadTxn txn) {}

    @Override
    public void validateTaskOutput(TaskKey currentTaskKey, @Nullable Serializable output, StoreReadTxn txn) {}
}
