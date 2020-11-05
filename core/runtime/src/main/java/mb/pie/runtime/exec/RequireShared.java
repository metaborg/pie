package mb.pie.runtime.exec;

import mb.pie.api.Tracer;
import mb.pie.api.InconsistentResourceProvide;
import mb.pie.api.InconsistentResourceRequire;
import mb.pie.api.InconsistentTaskReq;
import mb.pie.api.ResourceProvideDep;
import mb.pie.api.ResourceRequireDep;
import mb.pie.api.Store;
import mb.pie.api.StoreReadTxn;
import mb.pie.api.Task;
import mb.pie.api.TaskData;
import mb.pie.api.TaskDefs;
import mb.pie.api.TaskKey;
import mb.pie.api.TaskRequireDep;
import mb.pie.api.exec.CancelToken;
import mb.resource.ResourceService;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.HashMap;

public class RequireShared {
    private final TaskDefs taskDefs;
    private final ResourceService resourceService;
    private final Store store;
    private final Tracer tracer;

    private final HashMap<TaskKey, TaskData> visited;

    public RequireShared(
        TaskDefs taskDefs,
        ResourceService resourceService,
        Store store,
        Tracer tracer,
        HashMap<TaskKey, TaskData> visited
    ) {
        this.taskDefs = taskDefs;
        this.resourceService = resourceService;
        this.store = store;
        this.tracer = tracer;

        this.visited = visited;
    }

    /**
     * Attempt to get task data from the visited cache.
     */
    @Nullable TaskData dataFromVisited(TaskKey key) {
        tracer.checkVisitedStart(key);
        final @Nullable TaskData data = visited.get(key);
        tracer.checkVisitedEnd(key, data != null ? data.output : null);
        return data;
    }

    /**
     * Attempt to get task data from the store.
     */
    @Nullable TaskData dataFromStore(TaskKey key) {
        tracer.checkStoredStart(key);
        final @Nullable TaskData data;
        try(final StoreReadTxn txn = store.readTxn()) {
            data = txn.data(key);
        }
        tracer.checkStoredEnd(key, data != null ? data.output : null);
        return data;
    }


    /**
     * Check if input is internally consistent.
     */
    @Nullable InconsistentInput checkInput(Serializable input, Task<?> task) {
        if(!input.equals(task.input)) {
            return new InconsistentInput(input, task.input);
        }
        return null;
    }

    /**
     * Check if output is internally consistent.
     */
    @Nullable InconsistentTransientOutput checkOutputConsistency(@Nullable Serializable output) {
        return InconsistentTransientOutput.checkOutput(output);
    }

    /**
     * Check if a resource require dependency is internally consistent.
     */
    @Nullable InconsistentResourceRequire checkResourceRequireDep(TaskKey key, Task<?> task, ResourceRequireDep resourceRequireDep) {
        tracer.checkResourceRequireStart(key, task, resourceRequireDep);
        final @Nullable InconsistentResourceRequire reason = resourceRequireDep.checkConsistency(resourceService);
        tracer.checkResourceRequireEnd(key, task, resourceRequireDep, reason);
        return reason;
    }

    /**
     * Check if a resource provide dependency is internally consistent.
     */
    @Nullable InconsistentResourceProvide checkResourceProvideDep(TaskKey key, Task<?> task, ResourceProvideDep resourceProvideDep) {
        tracer.checkResourceProvideStart(key, task, resourceProvideDep);
        final @Nullable InconsistentResourceProvide reason = resourceProvideDep.checkConsistency(resourceService);
        tracer.checkResourceProvideEnd(key, task, resourceProvideDep, reason);
        return reason;
    }

    /**
     * Check if a task require dependency is totally consistent.
     */
    @Nullable InconsistentTaskReq checkTaskRequireDep(TaskKey key, Task<?> task, TaskRequireDep taskRequireDep, RequireTask requireTask, boolean modifyObservability, CancelToken cancel) {
        final TaskKey calleeKey = taskRequireDep.callee;
        final Task<?> calleeTask;
        try(final StoreReadTxn txn = store.readTxn()) {
            calleeTask = calleeKey.toTask(taskDefs, txn);
        }
        final @Nullable Serializable calleeOutput = requireTask.require(calleeKey, calleeTask, modifyObservability, cancel);
        tracer.checkTaskRequireStart(key, task, taskRequireDep);
        final @Nullable InconsistentTaskReq reason = taskRequireDep.checkConsistency(calleeOutput);
        tracer.checkTaskRequireEnd(key, task, taskRequireDep, reason);
        return reason;
    }
}
