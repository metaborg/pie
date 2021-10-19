package mb.pie.runtime.exec;

import mb.log.api.LoggerFactory;
import mb.pie.api.Callbacks;
import mb.pie.api.Layer;
import mb.pie.api.Observability;
import mb.pie.api.Share;
import mb.pie.api.StoreWriteTxn;
import mb.pie.api.Task;
import mb.pie.api.TaskData;
import mb.pie.api.TaskDefs;
import mb.pie.api.TaskDeps;
import mb.pie.api.TaskKey;
import mb.pie.api.TaskRequireDep;
import mb.pie.api.Tracer;
import mb.pie.api.UncheckedExecException;
import mb.pie.api.exec.CancelToken;
import mb.pie.api.exec.CanceledException;
import mb.pie.api.exec.ExecReason;
import mb.pie.api.exec.UncheckedInterruptedException;
import mb.pie.runtime.DefaultStampers;
import mb.pie.runtime.share.NonSharingShare;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
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
    private final HashSet<ResourceKey> providedResources;

    public TaskExecutor(
        TaskDefs taskDefs,
        ResourceService resourceService,
        Share share,
        DefaultStampers defaultStampers,
        Layer layer,
        LoggerFactory loggerFactory,
        Tracer tracer,
        Callbacks callbacks,
        HashMap<TaskKey, TaskData> visited,
        HashSet<ResourceKey> providedResources
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
        this.providedResources = providedResources;
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

        // Reset task and obtain its previous data (or null if the task did not exist before)
        final @Nullable TaskData previousData = txn.resetTask(task);
        final Observability previousObservability = previousData != null ? previousData.taskObservability : Observability.Unobserved;
        final Collection<TaskRequireDep> previousTaskRequireDeps = previousData != null ? previousData.deps.taskRequireDeps : Collections.emptySet();

        // Execute the task.
        final ExecContextImpl context = new ExecContextImpl(
            taskDefs,
            resourceService,
            defaultStampers,
            layer,
            loggerFactory,
            tracer,

            key,
            previousData,

            txn,
            requireTask,
            modifyObservability || previousObservability.isObserved(),
            cancel
        );
        final @Nullable Serializable output;
        try {
            tracer.executeStart(key, task, reason);
            output = task.exec(context);
        } catch(UncheckedInterruptedException e) {
            // Special case for UncheckedInterruptedException which can occur with nested execution.
            txn.restoreData(key, previousData); // Restore previous data on cancel.
            tracer.executeEndInterrupted(key, task, reason, e.interruptedException);
            Thread.currentThread().interrupt(); // Interrupt the current thread.
            throw e; // Propagate UncheckedInterruptedExceptions, no need to wrap them.
        } catch(CanceledException e) {
            txn.restoreData(key, previousData); // Restore previous data on cancel.
            // TODO: log cancel
            throw e; // Propagate CanceledExceptions, no need to wrap them.
        } catch(RuntimeException e) {
            txn.restoreData(key, previousData); // Restore previous data on failure.
            tracer.executeEndFailed(key, task, reason, e);
            throw e; // Propagate RuntimeExceptions, no need to wrap them.
        } catch(InterruptedException e) {
            txn.restoreData(key, previousData); // Restore previous data on cancel.
            tracer.executeEndInterrupted(key, task, reason, e);
            Thread.currentThread().interrupt(); // Interrupt the current thread.
            // Turn InterruptedExceptions into UncheckedInterruptedException and propagate.
            throw new UncheckedInterruptedException(e);
        } catch(Exception e) {
            txn.restoreData(key, previousData); // Restore previous data on failure.
            tracer.executeEndFailed(key, task, reason, e);
            // Wrap regular exceptions into an UncheckedExecException which is propagated up to the entry point, where
            // it will be turned into an ExecException that must be handled by the caller.
            throw new UncheckedExecException("Executing task '" + task.desc(100) + "' failed unexpectedly", e);
        } catch(Throwable e) {
            txn.restoreData(key, previousData); // Restore previous data on failure.
            // TODO: log throwable
            throw e; // Propagate Throwables, no need to wrap them.
        }

        // Gather task data.
        final Observability newObservability;
        if(modifyObservability && previousObservability.isUnobserved()) {
            // Set to observed if previously unobserved (and we are allowed to modify observability).
            newObservability = Observability.ImplicitObserved;
            tracer.setTaskObservability(key, previousObservability, newObservability);
        } else {
            // Copy observability otherwise.
            newObservability = previousObservability;
        }
        final TaskDeps deps = context.getDeps();
        deps.resourceProvideDeps.forEach(d -> providedResources.add(d.key));
        final TaskData data = new TaskData(task.input, txn.getInternalObject(key), output, newObservability, deps);
        tracer.executeEndSuccess(key, task, reason, data);

        // Validate task output, then write the output and set the new observability.
        layer.validateTaskOutput(key, output, txn);
        txn.setOutput(key, output);
        txn.setTaskObservability(key, newObservability);

        // Implicitly unobserve tasks which the executed task no longer depends on (if this task is observed).
        if(newObservability.isObserved()) {
            final HashSet<TaskKey> newRequiredTasks = deps.taskRequireDeps.stream().map((d) -> d.callee).collect(Collectors.toCollection(HashSet::new));
            for(TaskRequireDep dep : previousTaskRequireDeps) {
                if(!newRequiredTasks.contains(dep.callee)) {
                    Observability.implicitUnobserve(txn, dep.callee, tracer);
                }
            }
        }

        // Mark as visited.
        visited.put(key, data);

        // Invoke callback, if any.
        final @Nullable Consumer<@Nullable Serializable> callback = callbacks.get(key, txn);
        if(callback != null) {
            tracer.invokeCallbackStart(callback, key, output);
            callback.accept(output);
            tracer.invokeCallbackEnd(callback, key, output);
        }

        return data;
    }
}
