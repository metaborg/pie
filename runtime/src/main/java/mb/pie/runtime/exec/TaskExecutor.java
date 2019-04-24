package mb.pie.runtime.exec;

import mb.pie.api.*;
import mb.pie.api.exec.Cancelled;
import mb.pie.api.exec.ExecReason;
import mb.pie.runtime.DefaultStampers;
import mb.pie.runtime.share.NonSharingShare;
import mb.resource.ResourceService;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class TaskExecutor {
    private final TaskDefs taskDefs;
    private final ResourceService resourceService;
    private final Store store;
    private final Share share;
    private final DefaultStampers defaultStampers;
    private final Layer layer;
    private final Logger logger;
    private final ExecutorLogger executorLogger;
    private final ConcurrentHashMap<TaskKey, Consumer<@Nullable Serializable>> observers;

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
        ConcurrentHashMap<TaskKey, Consumer<@Nullable Serializable>> observers,
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
        this.observers = observers;

        this.visited = visited;
    }

    TaskData exec(TaskKey key, Task<?> task, ExecReason reason, RequireTask requireTask, Cancelled cancel) throws ExecException, InterruptedException {
        cancel.throwIfCancelled();
        executorLogger.executeStart(key, task, reason);
        final TaskData data;
        if(share instanceof NonSharingShare) {
            // PERF HACK: circumvent share if it is a NonSharingShare for performance.
            data = execInternal(key, task, requireTask, cancel);
        } else {
            try {
                data = share.share(key, () -> {
                    try {
                        return execInternal(key, task, requireTask, cancel);
                    } catch(InterruptedException | ExecException e) {
                        throw new RuntimeException(e);
                    }
                }, () -> visited.get(key));
            } catch(RuntimeException e) {
                final Throwable cause = e.getCause();
                if(cause instanceof InterruptedException) {
                    throw (InterruptedException) cause;
                } else if(cause instanceof ExecException) {
                    throw (ExecException) cause;
                } else {
                    throw e;
                }
            }
        }
        executorLogger.executeEnd(key, task, reason, data);
        return data;
    }

    private TaskData execInternal(TaskKey key, Task<?> task, RequireTask requireTask, Cancelled cancel) throws ExecException, InterruptedException {
        cancel.throwIfCancelled();
        // Execute
        final ExecContextImpl context =
            new ExecContextImpl(requireTask, cancel, taskDefs, resourceService, store, defaultStampers, logger);
        final @Nullable Serializable output;
        try {
            output = task.exec(context);
        } catch(InterruptedException | RuntimeException | ExecException e) {
            // Propagate interrupted exceptions, these must be handled by the caller.
            // Propagate runtime exceptions, because we cannot recover from runtime exceptions here, but the caller may.
            // Propagate exec exceptions, no need to wrap them.
            throw e;
        } catch(Exception e) {
            // Wrap regular exceptions into an ExecException which is propagated up, and must be handled by the user.
            throw new ExecException("Executing task " + task.desc(100) + " failed unexpectedly", e);
        }
        Stats.addExecution();
        final ExecContextImpl.Deps deps = context.deps();
        final TaskData data =
            new TaskData(task.input, output, deps.taskRequires, deps.resourceRequires, deps.resourceProvides);
        // Validate well-formedness of the dependency graph, before writing.
        try(final StoreReadTxn txn = store.readTxn()) {
            layer.validatePreWrite(key, data, txn);
        }
        // Write output and dependencies to the store.
        try(final StoreWriteTxn txn = store.writeTxn()) {
            txn.setData(key, data);
        }
        // Validate well-formedness of the dependency graph, after writing.
        try(final StoreReadTxn txn = store.readTxn()) {
            layer.validatePostWrite(key, data, txn);
        }
        // Mark as visited.
        visited.put(key, data);
        // Notify observer, if any.
        final @Nullable Consumer<@Nullable Serializable> observer = observers.get(key);
        if(observer != null) {
            executorLogger.invokeObserverStart(observer, key, output);
            observer.accept(output);
            executorLogger.invokeObserverEnd(observer, key, output);
        }
        return data;
    }
}
