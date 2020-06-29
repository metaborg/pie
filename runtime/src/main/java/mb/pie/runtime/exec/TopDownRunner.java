package mb.pie.runtime.exec;

import mb.pie.api.ExecutorLogger;
import mb.pie.api.InconsistentResourceProvide;
import mb.pie.api.InconsistentResourceRequire;
import mb.pie.api.InconsistentTaskReq;
import mb.pie.api.Layer;
import mb.pie.api.Observability;
import mb.pie.api.ResourceProvideDep;
import mb.pie.api.ResourceRequireDep;
import mb.pie.api.Store;
import mb.pie.api.StoreReadTxn;
import mb.pie.api.StoreWriteTxn;
import mb.pie.api.Task;
import mb.pie.api.TaskData;
import mb.pie.api.TaskKey;
import mb.pie.api.TaskRequireDep;
import mb.pie.api.exec.CancelToken;
import mb.pie.api.exec.ExecReason;
import mb.pie.runtime.Callbacks;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.function.Consumer;

public class TopDownRunner implements RequireTask {
    private final Store store;
    private final Layer layer;
    private final ExecutorLogger executorLogger;
    private final TaskExecutor taskExecutor;
    private final RequireShared requireShared;
    private final Callbacks callbacks;

    private final HashMap<TaskKey, TaskData> visited;

    public TopDownRunner(
        Store store,
        Layer layer,
        ExecutorLogger executorLogger,
        TaskExecutor taskExecutor,
        RequireShared requireShared,
        Callbacks callbacks,
        HashMap<TaskKey, TaskData> visited
    ) {
        this.store = store;
        this.layer = layer;
        this.executorLogger = executorLogger;
        this.taskExecutor = taskExecutor;
        this.requireShared = requireShared;
        this.callbacks = callbacks;

        this.visited = visited;
    }

    public <O extends @Nullable Serializable> O requireInitial(Task<O> task, boolean modifyObservability, CancelToken cancel) {
        final TaskKey key = task.key();
        executorLogger.requireTopDownInitialStart(key, task);
        final O output = require(key, task, modifyObservability, cancel);
        if(modifyObservability) {
            try(StoreWriteTxn txn = store.writeTxn()) {
                // Set task as root observable when required initially.
                txn.setTaskObservability(key, Observability.ExplicitObserved);
            }
        }
        executorLogger.requireTopDownInitialEnd(key, task, output);
        return output;
    }

    @Override
    public <O extends @Nullable Serializable> O require(TaskKey key, Task<O> task, boolean modifyObservability, CancelToken cancel) {
        cancel.throwIfCanceled();
        Stats.addRequires();
        layer.requireTopDownStart(key, task.input);
        executorLogger.requireTopDownStart(key, task);
        try {
            final DataAndExecutionStatus status = executeOrGetExisting(key, task, modifyObservability, cancel);
            TaskData data = status.data;
            @SuppressWarnings({"unchecked"}) final O output = (O)data.output;
            if(!status.executed) {
                if(modifyObservability && data.taskObservability.isUnobserved()) {
                    // Force observability status to observed in task data, so that validation and the visited map contain a consistent TaskData object.
                    data = data.withTaskObservability(Observability.ImplicitObserved);
                    // Validate well-formedness of the dependency graph, and set task to observed.
                    try(final StoreWriteTxn txn = store.writeTxn()) {
                        layer.validatePostWrite(key, data, txn);
                        txn.setTaskObservability(key, Observability.ImplicitObserved);
                    }
                } else { // PERF: duplicate code to prevent creation of two transactions.
                    // Validate well-formedness of the dependency graph.
                    try(final StoreReadTxn txn = store.readTxn()) {
                        layer.validatePostWrite(key, data, txn);
                    }
                }

                // Mark task as visited.
                visited.put(key, data);

                // Invoke callback, if any.
                final @Nullable Consumer<@Nullable Serializable> callback = callbacks.get(key);
                if(callback != null) {
                    executorLogger.invokeCallbackStart(callback, key, output);
                    callback.accept(output);
                    executorLogger.invokeCallbackEnd(callback, key, output);
                }
            }
            executorLogger.requireTopDownEnd(key, task, output);
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
    private DataAndExecutionStatus executeOrGetExisting(TaskKey key, Task<?> task, boolean modifyObservability, CancelToken cancel) {
        // Check if task was already visited this execution.
        final @Nullable TaskData visitedData = requireShared.dataFromVisited(key);
        if(visitedData != null) {
            // Validate required task against visited data.
            layer.validateVisited(key, task, visitedData);
            // If validation succeeds, return immediately.
            return new DataAndExecutionStatus(visitedData, false);
        }

        // Check if data is stored for task. Execute if not.
        final @Nullable TaskData storedData = requireShared.dataFromStore(key);
        if(storedData == null) {
            return new DataAndExecutionStatus(exec(key, task, new NoData(), modifyObservability, cancel), true);
        }

        // Check consistency of task.
        // Input consistency.
        {
            final @Nullable InconsistentInput reason = requireShared.checkInput(storedData.input, task);
            if(reason != null) {
                return new DataAndExecutionStatus(exec(key, task, reason, modifyObservability, cancel), true);
            }
        }

        // Output consistency.
        {
            final @Nullable InconsistentTransientOutput reason =
                requireShared.checkOutputConsistency(storedData.output);
            if(reason != null) {
                return new DataAndExecutionStatus(exec(key, task, reason, modifyObservability, cancel), true);
            }
        }

        // Resource require consistency.
        for(ResourceRequireDep resourceRequireDep : storedData.resourceRequires) {
            final @Nullable InconsistentResourceRequire reason =
                requireShared.checkResourceRequireDep(key, task, resourceRequireDep);
            if(reason != null) {
                return new DataAndExecutionStatus(exec(key, task, reason, modifyObservability, cancel), true);
            }
        }

        // Resource provide consistency.
        for(ResourceProvideDep resourceProvideDep : storedData.resourceProvides) {
            final @Nullable InconsistentResourceProvide reason =
                requireShared.checkResourceProvideDep(key, task, resourceProvideDep);
            if(reason != null) {
                return new DataAndExecutionStatus(exec(key, task, reason, modifyObservability, cancel), true);
            }
        }

        // Task require consistency.
        for(TaskRequireDep taskRequireDep : storedData.taskRequires) {
            final @Nullable InconsistentTaskReq reason =
                requireShared.checkTaskRequireDep(key, task, taskRequireDep, this, modifyObservability, cancel);
            if(reason != null) {
                return new DataAndExecutionStatus(exec(key, task, reason, modifyObservability, cancel), true);
            }
        }

        // Task is consistent.
        return new DataAndExecutionStatus(storedData, false);
    }

    public TaskData exec(TaskKey key, Task<?> task, ExecReason reason, boolean modifyObservability, CancelToken cancel) {
        return taskExecutor.exec(key, task, reason, this, modifyObservability, cancel);
    }
}
