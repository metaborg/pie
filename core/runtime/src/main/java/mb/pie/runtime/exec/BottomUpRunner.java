package mb.pie.runtime.exec;

import mb.pie.api.Callbacks;
import mb.pie.api.InconsistentResourceProvide;
import mb.pie.api.InconsistentResourceRequire;
import mb.pie.api.InconsistentTaskRequire;
import mb.pie.api.Layer;
import mb.pie.api.Observability;
import mb.pie.api.ResourceProvideDep;
import mb.pie.api.ResourceRequireDep;
import mb.pie.api.Store;
import mb.pie.api.StoreReadTxn;
import mb.pie.api.StoreWriteTxn;
import mb.pie.api.Task;
import mb.pie.api.TaskData;
import mb.pie.api.TaskDefs;
import mb.pie.api.TaskKey;
import mb.pie.api.TaskRequireDep;
import mb.pie.api.Tracer;
import mb.pie.api.exec.CancelToken;
import mb.pie.api.exec.ExecReason;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class BottomUpRunner implements RequireTask {
    private final TaskDefs taskDefs;
    private final ResourceService resourceService;
    private final Store store;
    private final Layer layer;
    private final Tracer tracer;
    private final TaskExecutor taskExecutor;
    private final RequireShared requireShared;
    private final Callbacks callbacks;

    private final HashMap<TaskKey, TaskData> visited;
    private @MonotonicNonNull DistinctTaskKeyPriorityQueue scheduled;

    public BottomUpRunner(
        TaskDefs taskDefs,
        ResourceService resourceService,
        Store store,
        Layer layer,
        Tracer tracer,
        TaskExecutor taskExecutor,
        RequireShared requireShared,
        Callbacks callbacks,
        HashMap<TaskKey, TaskData> visited
    ) {
        this.taskDefs = taskDefs;
        this.resourceService = resourceService;
        this.store = store;
        this.layer = layer;
        this.tracer = tracer;
        this.taskExecutor = taskExecutor;
        this.requireShared = requireShared;
        this.callbacks = callbacks;

        this.visited = visited;
    }


    public void requireInitial(Set<? extends ResourceKey> changedResources, Set<?> tags, CancelToken cancel) {
        tracer.requireBottomUpInitialStart(changedResources);
        try(final StoreWriteTxn txn = store.writeTxn()) {
            scheduled = DistinctTaskKeyPriorityQueue.withTransitiveDependencyComparator(txn);
            for(TaskKey deferred : txn.deferredTasks()) {
                // Schedule deferred tasks that are observable and should not be deferred again.
                final Task<?> task = deferred.toTask(taskDefs, txn);
                if(txn.taskObservability(deferred).isObserved() && task.taskDef.shouldExecWhenAffected(task.input, tags)) {
                    tracer.scheduleTask(deferred);
                    scheduled.add(deferred);
                }
            }
            scheduleAffectedByResources(changedResources.stream(), txn);
            execScheduled(tags, true, txn, cancel); // Always modify observability in bottom-up build.
        } finally {
            tracer.requireBottomUpInitialEnd();
        }
    }

    public <O extends @Nullable Serializable> O requireInitial(Task<O> task, boolean modifyObservability, CancelToken cancel) {
        try(final StoreWriteTxn txn = store.writeTxn()) {
            scheduled = DistinctTaskKeyPriorityQueue.withTransitiveDependencyComparator(txn);
            final TaskKey key = task.key();
            tracer.requireTopDownInitialStart(key, task);
            final O output = require(key, task, modifyObservability, txn, cancel);
            if(modifyObservability) {
                // OPTO: can we make `require` set the desired observability?
                // Set task as explicitly observable when required initially in top-down fashion.
                final Observability previousObservability = txn.taskObservability(key);
                if(previousObservability != Observability.ExplicitObserved) {
                    final Observability newObservability = Observability.ExplicitObserved;
                    tracer.setTaskObservability(key, previousObservability, newObservability);
                    txn.setTaskObservability(key, newObservability);
                }
            }
            tracer.requireTopDownInitialEnd(key, task, output);
            return output;
        }
    }


    /**
     * Executes scheduled tasks (and schedules affected tasks) until queue is empty.
     */
    private void execScheduled(Set<?> tags, boolean modifyObservability, StoreWriteTxn txn, CancelToken cancel) {
        while(scheduled.isNotEmpty()) {
            cancel.throwIfCanceled();
            final TaskKey key = scheduled.poll();
            final Task<?> task = key.toTask(taskDefs, txn);
            if(task.taskDef.shouldExecWhenAffected(task.input, tags)) {
                execAndSchedule(key, task, new AffectedExecReason(), modifyObservability, txn, cancel);
            } else {
                tracer.deferTask(key);
                txn.addDeferredTask(key);
            }
        }
    }

    /**
     * Executes given task, and schedules new tasks based on given task's output.
     */
    private TaskData execAndSchedule(TaskKey key, Task<?> task, ExecReason reason, boolean modifyObservability, StoreWriteTxn txn, CancelToken cancel) {
        try {
            final TaskData data = exec(key, task, reason, modifyObservability, txn, cancel);
            scheduleAffectedByRequiredTask(key, data.output, txn);
            scheduleAffectedByRequiredResources(data.resourceProvides.stream().map((d) -> d.key), txn);
            return data;
        } finally {
            txn.removeDeferredTask(key);
        }
    }

    /**
     * Schedules tasks affected by (changes to) required and provided files.
     */
    private void scheduleAffectedByResources(Stream<? extends ResourceKey> resources, StoreReadTxn txn) {
        BottomUpShared.directlyAffectedByResources(resources, resourceService, txn, tracer, (key) -> {
            tracer.scheduleTask(key);
            scheduled.add(key);
        });
    }

    /**
     * Schedules tasks affected by (changes to) required files.
     */
    private void scheduleAffectedByRequiredResources(Stream<? extends ResourceKey> resources, StoreReadTxn txn) {
        resources.forEach((changedResource) -> {
            tracer.scheduleAffectedByResourceStart(changedResource);
            BottomUpShared.directlyAffectedByRequiredResource(changedResource, resourceService, txn, tracer, (key) -> {
                tracer.scheduleTask(key);
                scheduled.add(key);
            });
            tracer.scheduleAffectedByResourceEnd(changedResource);
        });
    }

    /**
     * Schedules tasks affected by (changes to the) output of a task.
     */
    private void scheduleAffectedByRequiredTask(TaskKey requiree, @Nullable Serializable output, StoreReadTxn txn) {
        BottomUpShared.directlyAffectedByRequiredTask(requiree, output, txn, tracer, (key) -> {
            tracer.scheduleTask(key);
            scheduled.add(key);
        });
    }


    /**
     * Require the result of a task.
     */
    @Override
    public <O extends @Nullable Serializable> O require(TaskKey key, Task<O> task, boolean modifyObservability, StoreWriteTxn txn, CancelToken cancel) {
        cancel.throwIfCanceled();
        layer.requireTopDownStart(key, task.input);
        try {
            final TaskData data = getData(key, task, modifyObservability, txn, cancel);
            @SuppressWarnings({"unchecked"}) final O output = (O)data.output;
            return output;
        } finally {
            layer.requireTopDownEnd(key);
        }
    }

    /**
     * Get data for given task/key, either by getting existing data or through execution.
     */
    private TaskData getData(TaskKey key, Task<?> task, boolean modifyObservability, StoreWriteTxn txn, CancelToken cancel) {
        // Check if task was already visited this execution.
        final @Nullable TaskData visitedData = requireShared.dataFromVisited(key);
        if(visitedData != null) {
            // Validate required task against visited data.
            layer.validateVisited(key, task, visitedData);
            // If validation succeeds, return immediately.
            return visitedData;
        }

        // Check if data is stored for task. Execute if not.
        final @Nullable TaskData storedData = requireShared.dataFromStore(key, txn);
        if(storedData == null) {
            // This task is new, therefore we execute it. We do not schedule tasks affected by this new task, since new
            // tasks cannot affect existing tasks.
            return exec(key, task, new NoData(), modifyObservability, txn, cancel);
        }

        // Task is in dependency graph, because we have stored data for it.

        if(storedData.taskObservability.isUnobserved()) {
            // Task is unobserved and therefore may not be consistent because unobserved tasks are not scheduled.
            // Require the unobserved task it in a top-down manner to make it consistent. We do not schedule tasks
            // affected by this unobserved task, since no observed task can depend on an unobserved task, thus no
            // observed task can be affected by this unobserved task.
            return requireUnobserved(key, task, storedData, modifyObservability, txn, cancel);
        }

        // The task is observed. It may be scheduled to be run, but we need its output *now*.
        final @Nullable TaskData requireNowData = requireScheduledNow(key, modifyObservability, txn, cancel);
        if(requireNowData != null) {
            // Task was scheduled. That is, it was either directly or indirectly affected. Therefore, it has been
            // executed, and we return the result of that execution.
            return requireNowData;
        } else {
            // Task was not scheduled. That is, it was not directly affected by resource changes, and not indirectly
            // affected by other tasks. Therefore, we did not execute it. However, the task may still be affected by
            // internal inconsistencies that require re-execution, which we will check now.

            // Internal input consistency changes.
            final Serializable input = storedData.input;
            {
                final @Nullable InconsistentInput reason = requireShared.checkInput(input, task);
                if(reason != null) {
                    return execAndSchedule(key, task, reason, modifyObservability, txn, cancel);
                }
            }

            // Internal transient consistency output consistency.
            final @Nullable Serializable output = storedData.output;
            {
                final @Nullable InconsistentTransientOutput reason = requireShared.checkOutputConsistency(output);
                if(reason != null) {
                    return execAndSchedule(key, task, reason, modifyObservability, txn, cancel);
                }
            }

            // Mark as visited.
            visited.put(key, storedData);

            // Invoke callback, if any.
            final @Nullable Consumer<@Nullable Serializable> callback = callbacks.get(key);
            if(callback != null) {
                tracer.invokeCallbackStart(callback, key, output);
                callback.accept(output);
                tracer.invokeCallbackEnd(callback, key, output);
            }

            tracer.upToDate(key, task);

            return storedData;
        }
    }

    private TaskData requireUnobserved(TaskKey key, Task<?> task, TaskData data, boolean modifyObservability, StoreWriteTxn txn, CancelToken cancel) {
        // Check consistency of task.
        try {
            tracer.checkTopDownStart(key, task);
            // Input consistency.
            {
                final @Nullable InconsistentInput reason = requireShared.checkInput(data.input, task);
                if(reason != null) {
                    return exec(key, task, reason, modifyObservability, txn, cancel);
                }
            }

            // Transient output consistency.
            {
                final @Nullable InconsistentTransientOutput reason =
                    requireShared.checkOutputConsistency(data.output);
                if(reason != null) {
                    return exec(key, task, reason, modifyObservability, txn, cancel);
                }
            }

            // Resource require consistency.
            for(ResourceRequireDep resourceRequireDep : data.resourceRequires) {
                final @Nullable InconsistentResourceRequire reason =
                    requireShared.checkResourceRequireDep(key, task, resourceRequireDep);
                if(reason != null) {
                    return exec(key, task, reason, modifyObservability, txn, cancel);
                }
            }

            // Resource provide consistency.
            for(ResourceProvideDep resourceProvideDep : data.resourceProvides) {
                final @Nullable InconsistentResourceProvide reason =
                    requireShared.checkResourceProvideDep(key, task, resourceProvideDep);
                if(reason != null) {
                    return exec(key, task, reason, modifyObservability, txn, cancel);
                }
            }

            // Task require consistency.
            for(TaskRequireDep taskRequireDep : data.taskRequires) {
                final @Nullable InconsistentTaskRequire reason =
                    requireShared.checkTaskRequireDep(key, task, taskRequireDep, modifyObservability, txn, this, cancel);
                if(reason != null) {
                    return exec(key, task, reason, modifyObservability, txn, cancel);
                }
            }
        } finally {
            tracer.checkTopDownEnd(key, task);
        }

        if(modifyObservability) {
            // Force observability status to observed in task data, so that validation and the visited map contain a consistent TaskData object.
            final Observability newObservability = Observability.ImplicitObserved;
            tracer.setTaskObservability(key, Observability.Unobserved, newObservability);
            data = data.withTaskObservability(newObservability);
            txn.setTaskObservability(key, newObservability);
        }

        // Validate well-formedness of the dependency graph.
        layer.validatePostWrite(key, data, txn);

        // Mark task as visited.
        visited.put(key, data);

        // Invoke callback, if any.
        final @Nullable Consumer<@Nullable Serializable> callback = callbacks.get(key);
        if(callback != null) {
            tracer.invokeCallbackStart(callback, key, data.output);
            callback.accept(data.output);
            tracer.invokeCallbackEnd(callback, key, data.output);
        }

        tracer.upToDate(key, task);

        return data;
    }

    /**
     * Execute the scheduled dependency of a task, and the task itself, which is required to be run *now*.
     */
    private @Nullable TaskData requireScheduledNow(TaskKey key, boolean modifyObservability, StoreWriteTxn txn, CancelToken cancel) {
        tracer.requireScheduledNowStart(key);
        while(scheduled.isNotEmpty()) {
            cancel.throwIfCanceled();
            final @Nullable TaskKey minTaskKey = scheduled.pollLeastTaskWithDepTo(key, txn);
            if(minTaskKey == null) {
                break;
            }
            final Task<?> minTask = minTaskKey.toTask(taskDefs, txn);
            final TaskData data = execAndSchedule(minTaskKey, minTask, new AffectedExecReason(), modifyObservability, txn, cancel);
            if(minTaskKey.equals(key)) {
                tracer.requireScheduledNowEnd(key, data);
                return data; // Task was affected, and has been executed: return result.
            }
        }
        tracer.requireScheduledNowEnd(key, null);
        return null; // Task was not affected: return null.
    }


    public TaskData exec(TaskKey key, Task<?> task, ExecReason reason, boolean modifyObservability, StoreWriteTxn txn, CancelToken cancel) {
        return taskExecutor.exec(key, task, reason, modifyObservability, txn, this, cancel);
    }
}
