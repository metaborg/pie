package mb.pie.runtime.exec;

import mb.pie.api.*;
import mb.pie.api.exec.Cancelled;
import mb.pie.api.exec.ExecReason;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class TopDownSession implements RequireTask {
    private final Store store;
    private final Layer layer;
    private final ExecutorLogger executorLogger;
    private final TaskExecutor taskExecutor;
    private final RequireShared requireShared;
    private final ConcurrentHashMap<TaskKey, Consumer<@Nullable Serializable>> observers;

    private final HashMap<TaskKey, TaskData> visited;

    public TopDownSession(
        Store store,
        Layer layer,
        ExecutorLogger executorLogger,
        TaskExecutor taskExecutor,
        RequireShared requireShared,
        ConcurrentHashMap<TaskKey, Consumer<@Nullable Serializable>> observers,
        HashMap<TaskKey, TaskData> visited
    ) {
        this.store = store;
        this.layer = layer;
        this.executorLogger = executorLogger;
        this.taskExecutor = taskExecutor;
        this.requireShared = requireShared;
        this.observers = observers;

        this.visited = visited;
    }

    public <O extends @Nullable Serializable> O requireInitial(Task<O> task, Cancelled cancel) throws ExecException, InterruptedException {
        try {
            final TaskKey key = task.key();
            executorLogger.requireTopDownInitialStart(key, task);
            final O output = require(key, task, cancel);
            executorLogger.requireTopDownInitialEnd(key, task, output);
            return output;
        } finally {
            store.sync();
        }
    }

    @Override
    public <O extends @Nullable Serializable> O require(TaskKey key, Task<O> task, Cancelled cancel) throws ExecException, InterruptedException {
        cancel.throwIfCancelled();
        Stats.addRequires();
        layer.requireTopDownStart(key, task.input);
        executorLogger.requireTopDownStart(key, task);
        try {
            final DataAndExecutionStatus status = executeOrGetExisting(key, task, cancel);
            final TaskData data = status.data;
            @SuppressWarnings("unchecked") final O output = (O) data.output;
            if(!status.executed) {
                // Validate well-formedness of the dependency graph.
                try(final StoreReadTxn txn = store.readTxn()) {
                    layer.validatePostWrite(key, data, txn);
                }
                // Mark task as visited.
                visited.put(key, data);
                // Notify observer, if any.
                final @Nullable Consumer<@Nullable Serializable> observer = observers.get(key);
                if(observer != null) {
                    executorLogger.invokeObserverStart(observer, key, output);
                    observer.accept(output);
                    executorLogger.invokeObserverEnd(observer, key, output);
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
        final Serializable input = storedData.input;
        final @Nullable Serializable output = storedData.output;
        final ArrayList<TaskRequireDep> taskRequires = storedData.taskRequires;
        final ArrayList<ResourceRequireDep> resourceRequires = storedData.resourceRequires;
        final ArrayList<ResourceProvideDep> resourceProvides = storedData.resourceProvides;
        // Internal input consistency changes.
        {
            final @Nullable InconsistentInput reason = requireShared.checkInput(input, task);
            if(reason != null) {
                return new DataAndExecutionStatus(exec(key, task, reason, cancel), true);
            }
        }
        // Internal transient consistency output consistency.
        {
            final @Nullable InconsistentTransientOutput reason = requireShared.checkOutputConsistency(output);
            if(reason != null) {
                return new DataAndExecutionStatus(exec(key, task, reason, cancel), true);
            }
        }
        // Internal resource consistency requires.
        for(ResourceRequireDep fileReq : resourceRequires) {
            final @Nullable InconsistentResourceRequire reason = requireShared.checkResourceRequire(key, task, fileReq);
            if(reason != null) {
                return new DataAndExecutionStatus(exec(key, task, reason, cancel), true);
            }
        }
        // Internal resource consistency provides.
        for(ResourceProvideDep fileGen : resourceProvides) {
            final @Nullable InconsistentResourceProvide reason = requireShared.checkResourceProvide(key, task, fileGen);
            if(reason != null) {
                return new DataAndExecutionStatus(exec(key, task, reason, cancel), true);
            }
        }
        // Total call consistency requirements.
        for(TaskRequireDep taskReq : taskRequires) {
            final @Nullable InconsistentTaskReq reason =
                requireShared.checkTaskRequire(key, task, taskReq, this, cancel);
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
