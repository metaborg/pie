package mb.pie.runtime.exec;

import mb.pie.api.*;
import mb.pie.api.exec.Cancelled;
import mb.pie.api.exec.ExecReason;
import mb.pie.api.fs.FileSystemResource;
import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamper;
import mb.pie.runtime.share.NonSharingShare;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.function.BiConsumer;

public class TaskExecutor {
    private final TaskDefs taskDefs;
    private final ResourceSystems resourceSystems;
    private final HashMap<TaskKey, TaskData<?, ?>> visited;
    private final Store store;
    private final Share share;
    private final OutputStamper defaultOutputStamper;
    private final ResourceStamper<FileSystemResource> defaultRequireFileSystemStamper;
    private final ResourceStamper<FileSystemResource> defaultProvideFileSystemStamper;
    private final Layer layer;
    private final Logger logger;
    private final ExecutorLogger executorLogger;
    private final @Nullable BiConsumer<TaskKey, TaskData<?, ?>> postExecFunc;

    public TaskExecutor(
        TaskDefs taskDefs,
        ResourceSystems resourceSystems,
        HashMap<TaskKey, TaskData<?, ?>> visited,
        Store store,
        Share share,
        OutputStamper defaultOutputStamper,
        ResourceStamper<FileSystemResource> defaultRequireFileSystemStamper,
        ResourceStamper<FileSystemResource> defaultProvideFileSystemStamper,
        Layer layer,
        Logger logger,
        ExecutorLogger executorLogger,
        @Nullable BiConsumer<TaskKey, TaskData<?, ?>> postExecFunc
    ) {
        this.taskDefs = taskDefs;
        this.resourceSystems = resourceSystems;
        this.visited = visited;
        this.store = store;
        this.share = share;
        this.defaultOutputStamper = defaultOutputStamper;
        this.defaultRequireFileSystemStamper = defaultRequireFileSystemStamper;
        this.defaultProvideFileSystemStamper = defaultProvideFileSystemStamper;
        this.layer = layer;
        this.logger = logger;
        this.executorLogger = executorLogger;
        this.postExecFunc = postExecFunc;
    }

    <I extends Serializable, O extends @Nullable Serializable> TaskData<I, O> exec(TaskKey key, Task<I, O> task, ExecReason reason, RequireTask requireTask, Cancelled cancel) throws ExecException, InterruptedException {
        cancel.throwIfCancelled();
        executorLogger.executeStart(key, task, reason);
        final TaskData<?, ?> data;
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
        return data.cast();
    }

    private <I extends Serializable, O extends @Nullable Serializable> TaskData<I, O> execInternal(TaskKey key, Task<I, O> task, RequireTask requireTask, Cancelled cancel) throws ExecException, InterruptedException {
        cancel.throwIfCancelled();
        // Execute
        final ExecContextImpl context = new ExecContextImpl(requireTask, cancel, taskDefs, resourceSystems, store, defaultOutputStamper, defaultRequireFileSystemStamper, defaultProvideFileSystemStamper, logger);
        final @Nullable O output = task.exec(context);
        Stats.addExecution();
        final ExecContextImpl.Deps deps = context.deps();
        final TaskData<I, O> data = new TaskData<>(task.input, output, deps.taskRequires, deps.resourceRequires, deps.resourceProvides);
        // Validate well-formedness of the dependency graph, before writing.
        try(final StoreReadTxn txn = store.readTxn()) {
            layer.validatePreWrite(key, data, txn);
        }
        // Call post-execution function.
        if(postExecFunc != null) {
            postExecFunc.accept(key, data);
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
        return data;
    }
}
