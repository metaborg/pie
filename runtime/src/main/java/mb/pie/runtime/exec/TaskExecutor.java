package mb.pie.runtime.exec;

import mb.pie.api.ExecutorLogger;
import mb.pie.api.Layer;
import mb.pie.api.Logger;
import mb.pie.api.Observability;
import mb.pie.api.Share;
import mb.pie.api.Store;
import mb.pie.api.StoreReadTxn;
import mb.pie.api.StoreWriteTxn;
import mb.pie.api.Task;
import mb.pie.api.TaskData;
import mb.pie.api.TaskDefs;
import mb.pie.api.TaskKey;
import mb.pie.api.UncheckedExecException;
import mb.pie.api.exec.CancelToken;
import mb.pie.api.exec.ExecReason;
import mb.pie.api.exec.UncheckedInterruptedException;
import mb.pie.runtime.Callbacks;
import mb.pie.runtime.DefaultStampers;
import mb.pie.runtime.share.NonSharingShare;
import mb.resource.ResourceService;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TaskExecutor {
    private final TaskDefs taskDefs;
    private final ResourceService resourceService;
    private final Store store;
    private final Share share;
    private final DefaultStampers defaultStampers;
    private final Layer layer;
    private final Logger logger;
    private final ExecutorLogger executorLogger;
    private final Callbacks callbacks;

    private final HashMap<TaskKey, TaskData> visited;

    public TaskExecutor(
        TaskDefs taskDefs,
        ResourceService resourceService,
        Store store,
        Share share,
        DefaultStampers defaultStampers,
        Layer layer,
        Logger logger,
        ExecutorLogger executorLogger,
        Callbacks callbacks,
        HashMap<TaskKey, TaskData> visited
    ) {
        this.taskDefs = taskDefs;
        this.resourceService = resourceService;
        this.store = store;
        this.share = share;
        this.defaultStampers = defaultStampers;
        this.layer = layer;
        this.logger = logger;
        this.executorLogger = executorLogger;
        this.callbacks = callbacks;

        this.visited = visited;
    }

    TaskData exec(
        TaskKey key,
        Task<?> task,
        ExecReason reason,
        RequireTask requireTask,
        boolean modifyObservability,
        CancelToken cancel
    ) {
        cancel.throwIfCanceled();
        executorLogger.executeStart(key, task, reason);
        final TaskData data;
        if(share instanceof NonSharingShare) {
            // PERF HACK: circumvent share if it is a NonSharingShare for performance.
            data = execInternal(key, task, requireTask, modifyObservability, cancel);
        } else {
            data = share.share(key, () -> execInternal(key, task, requireTask, modifyObservability, cancel), () -> visited.get(key));
        }
        executorLogger.executeEnd(key, task, reason, data);
        return data;
    }

    private TaskData execInternal(
        TaskKey key,
        Task<?> task,
        RequireTask requireTask,
        boolean modifyObservability,
        CancelToken cancel
    ) {
        cancel.throwIfCanceled();

        // Store previous data for observability comparison.
        final Observability previousObservability;
        final HashSet<TaskKey> previousTaskRequires;
        try(final StoreReadTxn txn = store.readTxn()) {
            // Graceful: returns Observability.Detached if task has no observability status yet.
            previousObservability = txn.taskObservability(key);
            // Graceful: returns empty list if task has no task require dependencies yet.
            previousTaskRequires =
                txn.taskRequires(key).stream().map((d) -> d.callee).collect(Collectors.toCollection(HashSet::new));
        }

        // Execute the task.
        final ExecContextImpl context =
            new ExecContextImpl(requireTask, modifyObservability || previousObservability.isObserved(), cancel, taskDefs, resourceService, defaultStampers, logger);
        final @Nullable Serializable output;
        try {
            output = task.exec(context);
        } catch(RuntimeException e) {
            // Propagate runtime exceptions, no need to wrap them.
            throw e;
        } catch(InterruptedException e) {
            // Turn InterruptedExceptions into UncheckedInterruptedException.
            throw new UncheckedInterruptedException(e);
        } catch(Exception e) {
            // Wrap regular exceptions into an RuntimeExecException which is propagated up to the entry point, where it
            // will be turned into an ExecException that must be handled by the caller.
            throw new UncheckedExecException("Executing task '" + task.desc(100) + "' failed unexpectedly", e);
        }
        Stats.addExecution();

        // Gather task data.
        final Observability newObservability;
        if(modifyObservability && previousObservability.isUnobserved()) {
            // Set to observed if previously unobserved (and we are allowed to modify observability).
            newObservability = Observability.ImplicitObserved;
        } else {
            // Copy observability otherwise.
            newObservability = previousObservability;
        }
        final ExecContextImpl.Deps deps = context.deps();
        final TaskData data =
            new TaskData(task.input, output, newObservability, deps.taskRequires, deps.resourceRequires,
                deps.resourceProvides);

        // Validate, write data, and detach removed dependencies.
        try(final StoreWriteTxn txn = store.writeTxn()) {
            // Validate well-formedness of the dependency graph, before writing.
            layer.validatePreWrite(key, data, txn);

            // Write output and dependencies to the store.
            txn.setData(key, data);

            // Validate well-formedness of the dependency graph, after writing.
            layer.validatePostWrite(key, data, txn);

            // Implicitly unobserve tasks which the executed task no longer depends on (if this task is observed).
            if(newObservability.isObserved()) {
                final HashSet<TaskKey> newTaskRequires =
                    deps.taskRequires.stream().map((d) -> d.callee).collect(Collectors.toCollection(HashSet::new));
                previousTaskRequires.removeAll(newTaskRequires);
                for(TaskKey removedTaskRequire : previousTaskRequires) {
                    Observability.implicitUnobserve(txn, removedTaskRequire);
                }
            }
        }

        // Mark as visited.
        visited.put(key, data);

        // Invoke callback, if any.
        final @Nullable Consumer<@Nullable Serializable> callback = callbacks.get(key);
        if(callback != null) {
            executorLogger.invokeCallbackStart(callback, key, output);
            callback.accept(output);
            executorLogger.invokeCallbackEnd(callback, key, output);
        }

        return data;
    }
}
