package mb.pie.runtime.exec;

import mb.pie.api.*;
import mb.pie.api.exec.Cancelled;
import mb.pie.api.exec.ExecReason;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class TopDownSession implements RequireTask {
    private final Store store;
    private final Layer layer;
    private final ExecutorLogger executorLogger;
    private final TaskExecutor taskExecutor;
    private final RequireShared requireShared;
    private final ConcurrentHashMap<TaskKey, Consumer<@Nullable Serializable>> callbacks;

    private final HashMap<TaskKey, TaskData> visited;

    public TopDownSession(
        Store store,
        Layer layer,
        ExecutorLogger executorLogger,
        TaskExecutor taskExecutor,
        RequireShared requireShared,
        ConcurrentHashMap<TaskKey, Consumer<@Nullable Serializable>> callbacks,
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

    public <O extends @Nullable Serializable> O requireInitial(Task<O> task, Cancelled cancel) throws ExecException, InterruptedException {
        final TaskKey key = task.key();
        executorLogger.requireTopDownInitialStart(key, task);
        final O output = require(key, task, cancel);
        try(StoreWriteTxn txn = store.writeTxn()) {
            // Set task as root observable when required initially.
            txn.setTaskObservability(key, Observability.ExplicitObserved);
        }
        executorLogger.requireTopDownInitialEnd(key, task, output);
        return output;
    }

    @Override
    public <O extends @Nullable Serializable> O require(TaskKey key, Task<O> task, Cancelled cancel) throws ExecException, InterruptedException {
        cancel.throwIfCancelled();
        Stats.addRequires();
        layer.requireTopDownStart(key, task.input);
        executorLogger.requireTopDownStart(key, task);
        try {
            final DataAndExecutionStatus status = executeOrGetExisting(key, task, cancel);
            TaskData data = status.data;
            @SuppressWarnings("unchecked") final O output = (O) data.output;
            if(!status.executed) {
                if(data.taskObservability.isUnobserved()) {
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

    private class DataAndExecutionStatus {
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
    private DataAndExecutionStatus executeOrGetExisting(TaskKey key, Task<?> task, Cancelled cancel) throws ExecException, InterruptedException {
        // Check if task was already visited this execution. Return immediately if so.
        final @Nullable TaskData visitedData = requireShared.dataFromVisited(key);
        if(visitedData != null) {
            return new DataAndExecutionStatus(visitedData, false);
        }

        // Check if data is stored for task. Execute if not.
        final @Nullable TaskData storedData = requireShared.dataFromStore(key);
        if(storedData == null) {
            return new DataAndExecutionStatus(exec(key, task, new NoData(), cancel), true);
        }

        // Check consistency of task.
        // Input consistency.
        {
            final @Nullable InconsistentInput reason = requireShared.checkInput(storedData.input, task);
            if(reason != null) {
                return new DataAndExecutionStatus(exec(key, task, reason, cancel), true);
            }
        }

        // Output consistency.
        {
            final @Nullable InconsistentTransientOutput reason =
                requireShared.checkOutputConsistency(storedData.output);
            if(reason != null) {
                return new DataAndExecutionStatus(exec(key, task, reason, cancel), true);
            }
        }

        // Resource require consistency.
        for(ResourceRequireDep resourceRequireDep : storedData.resourceRequires) {
            final @Nullable InconsistentResourceRequire reason =
                requireShared.checkResourceRequireDep(key, task, resourceRequireDep);
            if(reason != null) {
                return new DataAndExecutionStatus(exec(key, task, reason, cancel), true);
            }
        }

        // Resource provide consistency.
        for(ResourceProvideDep resourceProvideDep : storedData.resourceProvides) {
            final @Nullable InconsistentResourceProvide reason =
                requireShared.checkResourceProvideDep(key, task, resourceProvideDep);
            if(reason != null) {
                return new DataAndExecutionStatus(exec(key, task, reason, cancel), true);
            }
        }

        // Task require consistency.
        for(TaskRequireDep taskRequireDep : storedData.taskRequires) {
            final @Nullable InconsistentTaskReq reason =
                requireShared.checkTaskRequireDep(key, task, taskRequireDep, this, cancel);
            if(reason != null) {
                return new DataAndExecutionStatus(exec(key, task, reason, cancel), true);
            }
        }

        // Task is consistent.
        return new DataAndExecutionStatus(storedData, false);
    }

    public TaskData exec(TaskKey key, Task<?> task, ExecReason reason, Cancelled cancel) throws ExecException, InterruptedException {
        return taskExecutor.exec(key, task, reason, this, cancel);
    }
}
