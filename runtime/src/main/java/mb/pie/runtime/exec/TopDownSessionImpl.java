package mb.pie.runtime.exec;

import mb.pie.api.*;
import mb.pie.api.exec.Cancelled;
import mb.pie.api.exec.ExecReason;
import mb.pie.api.exec.NullCancelled;
import mb.pie.api.exec.TopDownSession;
import mb.pie.runtime.DefaultStampers;
import mb.resource.ResourceRegistry;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class TopDownSessionImpl implements TopDownSession, RequireTask {
    private final Store store;
    private final Layer layer;
    private final ExecutorLogger executorLogger;
    private final TaskExecutor executor;
    private final RequireShared requireShared;

    private final HashMap<TaskKey, TaskData<?, ?>> visited = new HashMap<>();

    public TopDownSessionImpl(
        TaskDefs taskDefs,
        ResourceRegistry resourceRegistry,
        Store store,
        Share share,
        DefaultStampers defaultStampers,
        Layer layer,
        Logger logger,
        ExecutorLogger executorLogger
    ) {
        this.store = store;
        this.layer = layer;
        this.executorLogger = executorLogger;
        this.executor =
            new TaskExecutor(taskDefs, resourceRegistry, visited, store, share, defaultStampers, layer, logger,
                executorLogger, null);
        this.requireShared = new RequireShared(taskDefs, resourceRegistry, visited, store, executorLogger);
    }


    @Override
    public <I extends Serializable, O extends @Nullable Serializable> O requireInitial(Task<I, O> task) throws ExecException {
        try {
            return requireInitial(task, new NullCancelled());
        } catch(InterruptedException e) {
            // Cannot occur: NullCancelled is used, which does not check for interruptions.
            throw new RuntimeException(e);
        }
    }

    @Override
    public <I extends Serializable, O extends @Nullable Serializable> O requireInitial(Task<I, O> task, Cancelled cancel) throws ExecException, InterruptedException {
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
    public <I extends Serializable, O extends @Nullable Serializable> O require(TaskKey key, Task<I, O> task, Cancelled cancel) throws ExecException, InterruptedException {
        cancel.throwIfCancelled();
        Stats.addRequires();
        layer.requireTopDownStart(key, task.input);
        executorLogger.requireTopDownStart(key, task);
        try {
            final DataAndExecutionStatus<I, O> status = executeOrGetExisting(key, task, cancel);
            final TaskData<I, O> data = status.data;
            final O output = data.output;
            if(!status.executed) {
                // Validate well-formedness of the dependency graph.
                try(final StoreReadTxn txn = store.readTxn()) {
                    layer.validatePostWrite(key, data, txn);
                }
                // Mark task as visited.
                visited.put(key, data);
            }
            executorLogger.requireTopDownEnd(key, task, output);
            return output;
        } finally {
            layer.requireTopDownEnd(key);
        }
    }

    private class DataAndExecutionStatus<I extends Serializable, O extends @Nullable Serializable> {
        final TaskData<I, O> data;
        final boolean executed;

        private DataAndExecutionStatus(TaskData<I, O> data, boolean executed) {
            this.data = data;
            this.executed = executed;
        }
    }

    /**
     * Get data for given task/key, either by getting existing data or through execution.
     */
    private <I extends Serializable, O extends @Nullable Serializable> DataAndExecutionStatus<I, O> executeOrGetExisting(TaskKey key, Task<I, O> task, Cancelled cancel) throws ExecException, InterruptedException {
        // Check if task was already visited this execution. Return immediately if so.
        final @Nullable TaskData<?, ?> visitedData = requireShared.dataFromVisited(key);
        if(visitedData != null) {
            return new DataAndExecutionStatus<>(visitedData.cast(), false);
        }

        // Check if data is stored for task. Execute if not.
        final @Nullable TaskData<?, ?> storedData = requireShared.dataFromStore(key);
        if(storedData == null) {
            return new DataAndExecutionStatus<>(exec(key, task, new NoData(), cancel), true);
        }

        // Check consistency of task.
        final TaskData<I, O> existingData = storedData.cast();
        final I input = existingData.input;
        final @Nullable O output = existingData.output;
        final ArrayList<TaskRequireDep> taskRequires = existingData.taskRequires;
        final ArrayList<ResourceRequireDep> resourceRequires = existingData.resourceRequires;
        final ArrayList<ResourceProvideDep> resourceProvides = existingData.resourceProvides;

        // Internal input consistency changes.
        {
            final @Nullable InconsistentInput reason = requireShared.checkInput(input, task);
            if(reason != null) {
                return new DataAndExecutionStatus<>(exec(key, task, reason, cancel), true);
            }
        }

        // Internal transient consistency output consistency.
        {
            final @Nullable InconsistentTransientOutput reason = requireShared.checkOutputConsistency(output);
            if(reason != null) {
                return new DataAndExecutionStatus<>(exec(key, task, reason, cancel), true);
            }
        }

        // Internal resource consistency requires.
        for(ResourceRequireDep fileReq : resourceRequires) {
            final @Nullable InconsistentResourceRequire reason = requireShared.checkResourceRequire(key, task, fileReq);
            if(reason != null) {
                return new DataAndExecutionStatus<>(exec(key, task, reason, cancel), true);
            }
        }

        // Internal resource consistency provides.
        for(ResourceProvideDep fileGen : resourceProvides) {
            final @Nullable InconsistentResourceProvide reason = requireShared.checkResourceProvide(key, task, fileGen);
            if(reason != null) {
                return new DataAndExecutionStatus<>(exec(key, task, reason, cancel), true);
            }
        }

        // Total call consistency requirements.
        for(TaskRequireDep taskReq : taskRequires) {
            final @Nullable InconsistentTaskReq reason =
                requireShared.checkTaskRequire(key, task, taskReq, this, cancel);
            if(reason != null) {
                return new DataAndExecutionStatus<>(exec(key, task, reason, cancel), true);
            }
        }

        // Task is consistent.
        return new DataAndExecutionStatus<>(existingData, false);
    }

    public <I extends Serializable, O extends @Nullable Serializable> TaskData<I, O> exec(TaskKey key, Task<I, O> task, ExecReason reason, Cancelled cancel) throws ExecException, InterruptedException {
        return executor.exec(key, task, reason, this, cancel);
    }
}
