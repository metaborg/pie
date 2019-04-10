package mb.pie.runtime.exec;

import mb.pie.api.*;
import mb.pie.api.exec.Cancelled;
import mb.resource.ResourceRegistry;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.HashMap;

public class RequireShared {
    private final TaskDefs taskDefs;
    private final ResourceRegistry resourceRegistry;
    private final HashMap<TaskKey, TaskData<?, ?>> visited;
    private final Store store;
    private final ExecutorLogger executorLogger;

    public RequireShared(TaskDefs taskDefs, ResourceRegistry resourceRegistry, HashMap<TaskKey, TaskData<?, ?>> visited, Store store, ExecutorLogger executorLogger) {
        this.taskDefs = taskDefs;
        this.resourceRegistry = resourceRegistry;
        this.visited = visited;
        this.store = store;
        this.executorLogger = executorLogger;
    }


    /**
     * Attempt to get task data from the visited cache.
     */
    public @Nullable TaskData<?, ?> dataFromVisited(TaskKey key) {
        executorLogger.checkVisitedStart(key);
        final @Nullable TaskData<?, ?> data = visited.get(key);
        executorLogger.checkVisitedEnd(key, data != null ? data.output : null);
        return data;
    }

    /**
     * Attempt to get task data from the store.
     */
    public @Nullable TaskData<?, ?> dataFromStore(TaskKey key) {
        executorLogger.checkStoredStart(key);
        final @Nullable TaskData<?, ?> data;
        try(final StoreReadTxn txn = store.readTxn()) {
            data = txn.data(key);
        }
        executorLogger.checkStoredEnd(key, data != null ? data.output : null);
        return data;
    }


    /**
     * Check if input is internally consistent.
     */
    public @Nullable InconsistentInput checkInput(Serializable input, Task<?, ?> task) {
        if(!input.equals(task.input)) {
            return new InconsistentInput(input, task.input);
        }
        return null;
    }

    /**
     * Check if output is internally consistent.
     */
    public @Nullable InconsistentTransientOutput checkOutputConsistency(@Nullable Serializable output) {
        return InconsistentTransientOutput.checkOutput(output);
    }

    /**
     * Check if a file requires dependency is internally consistent.
     */
    public @Nullable InconsistentResourceRequire checkResourceRequire(TaskKey key, Task<?, ?> task, ResourceRequireDep fileReq) {
        executorLogger.checkResourceRequireStart(key, task, fileReq);
        final @Nullable InconsistentResourceRequire reason = fileReq.checkConsistency(resourceRegistry);
        executorLogger.checkResourceRequireEnd(key, task, fileReq, reason);
        return reason;
    }

    /**
     * Check if a file generates dependency is internally consistent.
     */
    public @Nullable InconsistentResourceProvide checkResourceProvide(TaskKey key, Task<?, ?> task, ResourceProvideDep fileGen) {
        executorLogger.checkResourceProvideStart(key, task, fileGen);
        final @Nullable InconsistentResourceProvide reason = fileGen.checkConsistency(resourceRegistry);
        executorLogger.checkResourceProvideEnd(key, task, fileGen, reason);
        return reason;
    }

    /**
     * Check if a task requires dependency is totally consistent.
     */
    public @Nullable InconsistentTaskReq checkTaskRequire(TaskKey key, Task<?, ?> task, TaskRequireDep taskRequire, RequireTask requireTask, Cancelled cancel) throws ExecException, InterruptedException {
        final TaskKey calleeKey = taskRequire.callee;
        final Task<Serializable, @Nullable Serializable> calleeTask;
        try(final StoreReadTxn txn = store.readTxn()) {
            calleeTask = calleeKey.toTask(taskDefs, txn);
        }
        final @Nullable Serializable calleeOutput = requireTask.require(calleeKey, calleeTask, cancel);
        executorLogger.checkTaskRequireStart(key, task, taskRequire);
        final @Nullable InconsistentTaskReq reason = taskRequire.checkConsistency(calleeOutput);
        executorLogger.checkTaskRequireEnd(key, task, taskRequire, reason);
        return reason;
    }
}
