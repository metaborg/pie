package mb.pie.runtime.exec;

import mb.pie.api.Callbacks;
import mb.pie.api.InconsistentResourceProvide;
import mb.pie.api.InconsistentResourceRequire;
import mb.pie.api.InconsistentTaskRequire;
import mb.pie.api.Layer;
import mb.pie.api.Observability;
import mb.pie.api.ResourceProvideDep;
import mb.pie.api.ResourceRequireDep;
import mb.pie.api.Store;
import mb.pie.api.StoreWriteTxn;
import mb.pie.api.Task;
import mb.pie.api.TaskData;
import mb.pie.api.TaskKey;
import mb.pie.api.TaskRequireDep;
import mb.pie.api.Tracer;
import mb.pie.api.exec.CancelToken;
import mb.pie.api.exec.ExecReason;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.function.Consumer;

public class TopDownRunner implements RequireTask {
    private final Store store;
    private final Layer layer;
    private final Tracer tracer;
    private final TaskExecutor taskExecutor;
    private final RequireShared requireShared;
    private final Callbacks callbacks;

    private final HashMap<TaskKey, TaskData> visited;

    public TopDownRunner(
        Store store,
        Layer layer,
        Tracer tracer,
        TaskExecutor taskExecutor,
        RequireShared requireShared,
        Callbacks callbacks,
        HashMap<TaskKey, TaskData> visited
    ) {
        this.store = store;
        this.layer = layer;
        this.tracer = tracer;
        this.taskExecutor = taskExecutor;
        this.requireShared = requireShared;
        this.callbacks = callbacks;

        this.visited = visited;
    }

    public <O extends @Nullable Serializable> O requireInitial(Task<O> task, boolean modifyObservability, CancelToken cancel) {
        try(final StoreWriteTxn txn = store.writeTxn()) {
            final TaskKey key = task.key();
            tracer.requireTopDownInitialStart(key, task);
            final O output = require(key, task, modifyObservability, txn, cancel);
            if(modifyObservability) {
                // Set task as explicitly observable when required initially in top-down fashion.
                txn.setTaskObservability(key, Observability.ExplicitObserved);
            }
            tracer.requireTopDownInitialEnd(key, task, output);
            return output;
        }
    }

    @Override
    public <O extends @Nullable Serializable> O require(TaskKey key, Task<O> task, boolean modifyObservability, StoreWriteTxn txn, CancelToken cancel) {
        cancel.throwIfCanceled();
        layer.requireTopDownStart(key, task.input);
        try {
            final DataAndExecutionStatus status = executeOrGetExisting(key, task, modifyObservability, txn, cancel);
            TaskData data = status.data;
            @SuppressWarnings({"unchecked"}) final O output = (O)data.output;
            if(!status.executed) {
                if(modifyObservability && data.taskObservability.isUnobserved()) {
                    // Force observability status to observed in task data, so that validation and the visited map contain a consistent TaskData object.
                    data = data.withTaskObservability(Observability.ImplicitObserved);
                    txn.setTaskObservability(key, Observability.ImplicitObserved);
                }

                // Validate well-formedness of the dependency graph.
                layer.validatePostWrite(key, data, txn);

                // Mark task as visited.
                visited.put(key, data);

                // Invoke callback, if any.
                final @Nullable Consumer<@Nullable Serializable> callback = callbacks.get(key);
                if(callback != null) {
                    tracer.invokeCallbackStart(callback, key, output);
                    callback.accept(output);
                    tracer.invokeCallbackEnd(callback, key, output);
                }

                tracer.upToDate(key, task);
            }
            return output;
        } finally {
            layer.requireTopDownEnd(key);
        }
    }

    private static class DataAndExecutionStatus {
        final TaskData data;
        final boolean executed;

        private DataAndExecutionStatus(TaskData data, boolean executed) {
            this.data = data;
            this.executed = executed;
        }
    }

    /**
     * Get data for given task/key, either by getting existing data or through execution.
     */
    private DataAndExecutionStatus executeOrGetExisting(TaskKey key, Task<?> task, boolean modifyObservability, StoreWriteTxn txn, CancelToken cancel) {
        // Check if task was already visited this execution.
        final @Nullable TaskData visitedData = requireShared.dataFromVisited(key);
        if(visitedData != null) {
            // Validate required task against visited data.
            layer.validateVisited(key, task, visitedData);
            // If validation succeeds, return immediately.
            return new DataAndExecutionStatus(visitedData, false);
        }

        // Check if data is stored for task. Execute if not.
        final @Nullable TaskData storedData = requireShared.dataFromStore(key, txn);
        if(storedData == null) {
            return new DataAndExecutionStatus(exec(key, task, new NoData(), modifyObservability, txn, cancel), true);
        }

        // Check consistency of task.
        try {
            tracer.checkTopDownStart(key, task);

            // Input consistency.
            {
                final @Nullable InconsistentInput reason = requireShared.checkInput(storedData.input, task);
                if(reason != null) {
                    return new DataAndExecutionStatus(exec(key, task, reason, modifyObservability, txn, cancel), true);
                }
            }

            // Output consistency.
            {
                final @Nullable InconsistentTransientOutput reason = requireShared.checkOutputConsistency(storedData.output);
                if(reason != null) {
                    return new DataAndExecutionStatus(exec(key, task, reason, modifyObservability, txn, cancel), true);
                }
            }

            // Resource require consistency.
            for(ResourceRequireDep resourceRequireDep : storedData.resourceRequires) {
                final @Nullable InconsistentResourceRequire reason = requireShared.checkResourceRequireDep(key, task, resourceRequireDep);
                if(reason != null) {
                    return new DataAndExecutionStatus(exec(key, task, reason, modifyObservability, txn, cancel), true);
                }
            }

            // Resource provide consistency.
            for(ResourceProvideDep resourceProvideDep : storedData.resourceProvides) {
                final @Nullable InconsistentResourceProvide reason = requireShared.checkResourceProvideDep(key, task, resourceProvideDep);
                if(reason != null) {
                    return new DataAndExecutionStatus(exec(key, task, reason, modifyObservability, txn, cancel), true);
                }
            }

            // Task require consistency.
            for(TaskRequireDep taskRequireDep : storedData.taskRequires) {
                final @Nullable InconsistentTaskRequire reason = requireShared.checkTaskRequireDep(key, task, taskRequireDep, modifyObservability, txn, this, cancel);
                if(reason != null) {
                    return new DataAndExecutionStatus(exec(key, task, reason, modifyObservability, txn, cancel), true);
                }
            }

            // Task is consistent.
            return new DataAndExecutionStatus(storedData, false);
        } finally {
            tracer.checkTopDownEnd(key, task);
        }
    }

    public TaskData exec(TaskKey key, Task<?> task, ExecReason reason, boolean modifyObservability, StoreWriteTxn txn, CancelToken cancel) {
        return taskExecutor.exec(key, task, reason, modifyObservability, txn, this, cancel);
    }
}
