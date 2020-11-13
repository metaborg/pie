package mb.pie.runtime.exec;

import mb.log.api.LoggerFactory;
import mb.pie.api.Callbacks;
import mb.pie.api.Layer;
import mb.pie.api.Observability;
import mb.pie.api.Share;
import mb.pie.api.Store;
import mb.pie.api.StoreWriteTxn;
import mb.pie.api.Task;
import mb.pie.api.TaskData;
import mb.pie.api.TaskDefs;
import mb.pie.api.TaskKey;
import mb.pie.api.Tracer;
import mb.pie.api.UncheckedExecException;
import mb.pie.api.exec.CancelToken;
import mb.pie.api.exec.ExecReason;
import mb.pie.api.exec.UncheckedInterruptedException;
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
    private final Share share;
    private final DefaultStampers defaultStampers;
    private final Layer layer;
    private final LoggerFactory loggerFactory;
    private final Tracer tracer;
    private final Callbacks callbacks;

    private final HashMap<TaskKey, TaskData> visited;

    public TaskExecutor(
        TaskDefs taskDefs,
        ResourceService resourceService,
        Share share,
        DefaultStampers defaultStampers,
        Layer layer,
        LoggerFactory loggerFactory,
        Tracer tracer,
        Callbacks callbacks,
        HashMap<TaskKey, TaskData> visited
    ) {
        this.taskDefs = taskDefs;
        this.resourceService = resourceService;
        this.share = share;
        this.defaultStampers = defaultStampers;
        this.layer = layer;
        this.loggerFactory = loggerFactory;
        this.tracer = tracer;
        this.callbacks = callbacks;

        this.visited = visited;
    }

    TaskData exec(
        TaskKey key,
        Task<?> task,
        ExecReason reason,
        boolean modifyObservability,
        StoreWriteTxn txn,
        RequireTask requireTask,
        CancelToken cancel
    ) {
        cancel.throwIfCanceled();
        final TaskData data;
        if(share instanceof NonSharingShare) {
            // PERF HACK: circumvent share if it is a NonSharingShare for performance.
            data = execInternal(key, task, reason, modifyObservability, txn, requireTask, cancel);
        } else {
            data = share.share(key, () -> execInternal(key, task, reason, modifyObservability, txn, requireTask, cancel), () -> visited.get(key));
        }
        return data;
    }

    private TaskData execInternal(
        TaskKey key,
        Task<?> task,
        ExecReason reason,
        boolean modifyObservability,
        StoreWriteTxn txn,
        RequireTask requireTask,
        CancelToken cancel
    ) {
        cancel.throwIfCanceled();

        // Store previous data for observability comparison.
        // Graceful: returns Observability.Detached if task has no observability status yet.
        final Observability previousObservability = txn.taskObservability(key);
        // Graceful: returns empty list if task has no task require dependencies yet.
        final HashSet<TaskKey> previousTaskRequires = txn.taskRequires(key).stream().map((d) -> d.callee).collect(Collectors.toCollection(HashSet::new));

        // Execute the task.
        final ExecContextImpl context = new ExecContextImpl(txn, requireTask, modifyObservability || previousObservability.isObserved(), cancel, taskDefs, resourceService, defaultStampers, loggerFactory, tracer);
        final @Nullable Serializable output;
        try {
            tracer.executeStart(key, task, reason);
            output = task.exec(context);
        } catch(RuntimeException e) {
            tracer.executeEndFailed(key, task, reason, e);
            // Propagate runtime exceptions, no need to wrap them.
            throw e;
        } catch(InterruptedException e) {
            tracer.executeEndInterrupted(key, task, reason, e);
            // Turn InterruptedExceptions into UncheckedInterruptedException.
            throw new UncheckedInterruptedException(e);
        } catch(Exception e) {
            tracer.executeEndFailed(key, task, reason, e);
            // Wrap regular exceptions into an RuntimeExecException which is propagated up to the entry point, where it
            // will be turned into an ExecException that must be handled by the caller.
            throw new UncheckedExecException("Executing task '" + task.desc(100) + "' failed unexpectedly", e);
        }

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
        final TaskData data = new TaskData(task.input, output, newObservability, deps.taskRequires, deps.resourceRequires, deps.resourceProvides);
        tracer.executeEndSuccess(key, task, reason, data);

        // Validate well-formedness of the dependency graph, before writing.
        layer.validatePreWrite(key, data, txn);

        // Write output and dependencies to the store.
        txn.setData(key, data);

        // Validate well-formedness of the dependency graph, after writing.
        layer.validatePostWrite(key, data, txn);

        // Implicitly unobserve tasks which the executed task no longer depends on (if this task is observed).
        if(newObservability.isObserved()) {
            final HashSet<TaskKey> newTaskRequires = deps.taskRequires.stream().map((d) -> d.callee).collect(Collectors.toCollection(HashSet::new));
            previousTaskRequires.removeAll(newTaskRequires);
            for(TaskKey removedTaskRequire : previousTaskRequires) {
                Observability.implicitUnobserve(txn, removedTaskRequire);
            }
        }

        // Mark as visited.
        visited.put(key, data);

        // Invoke callback, if any.
        final @Nullable Consumer<@Nullable Serializable> callback = callbacks.get(key);
        if(callback != null) {
            tracer.invokeCallbackStart(callback, key, output);
            callback.accept(output);
            tracer.invokeCallbackEnd(callback, key, output);
        }

        return data;
    }
}
