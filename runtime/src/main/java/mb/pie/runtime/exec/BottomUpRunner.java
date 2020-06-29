package mb.pie.runtime.exec;

import mb.pie.api.ExecutorLogger;
import mb.pie.api.InconsistentResourceProvide;
import mb.pie.api.InconsistentResourceRequire;
import mb.pie.api.InconsistentTaskReq;
import mb.pie.api.Layer;
import mb.pie.api.Logger;
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
import mb.pie.api.exec.CancelToken;
import mb.pie.api.exec.ExecReason;
import mb.pie.runtime.Callbacks;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
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
    private final Logger logger;
    private final ExecutorLogger executorLogger;
    private final TaskExecutor taskExecutor;
    private final RequireShared requireShared;
    private final Callbacks callbacks;

    private final HashMap<TaskKey, TaskData> visited;
    private final DistinctTaskKeyPriorityQueue queue;

    public BottomUpRunner(
        TaskDefs taskDefs,
        ResourceService resourceService,
        Store store,
        Layer layer,
        Logger logger,
        ExecutorLogger executorLogger,
        TaskExecutor taskExecutor,
        RequireShared requireShared,
        Callbacks callbacks,
        HashMap<TaskKey, TaskData> visited
    ) {
        this.taskDefs = taskDefs;
        this.resourceService = resourceService;
        this.store = store;
        this.layer = layer;
        this.logger = logger;
        this.executorLogger = executorLogger;
        this.taskExecutor = taskExecutor;
        this.requireShared = requireShared;
        this.callbacks = callbacks;

        this.visited = visited;
        this.queue = DistinctTaskKeyPriorityQueue.withTransitiveDependencyComparator(store);
    }


    public void requireInitial(Set<? extends ResourceKey> changedResources, CancelToken cancel) {
        executorLogger.requireBottomUpInitialStart(changedResources);
        scheduleAffectedByResources(changedResources.stream());
        execScheduled(cancel);
        executorLogger.requireBottomUpInitialEnd();
    }


    /**
     * Executes scheduled tasks (and schedules affected tasks) until queue is empty.
     */
    private void execScheduled(CancelToken cancel) {
        logger.trace("Executing scheduled tasks: " + queue);
        while(queue.isNotEmpty()) {
            cancel.throwIfCanceled();
            final TaskKey key = queue.poll();
            final Task<?> task;
            try(final StoreReadTxn txn = store.readTxn()) {
                task = key.toTask(taskDefs, txn);
            }
            logger.trace("Polling: " + task.desc(200));
            execAndSchedule(key, task, cancel);
        }
    }

    /**
     * Executes given task, and schedules new tasks based on given task's output.
     */
    private TaskData execAndSchedule(TaskKey key, Task<?> task, CancelToken cancel) {
        final TaskData data = exec(key, task, new AffectedExecReason(), cancel);
        scheduleAffectedCallersOf(key, data.output);
        scheduleAffectedByRequiredResources(data.resourceProvides.stream().map((d) -> d.key));
        return data;
    }

    /**
     * Schedules tasks affected by (changes to) required and provided files.
     */
    private void scheduleAffectedByResources(Stream<? extends ResourceKey> resources) {
        logger.trace("Scheduling tasks affected by resources: " + resources);
        try(final StoreReadTxn txn = store.readTxn()) {
            BottomUpShared.directlyAffectedByResources(txn, resources, resourceService, logger, (key) -> {
                logger.trace("- scheduling: " + key);
                queue.add(key);
            });
        }
    }

    /**
     * Schedules tasks affected by (changes to) required files.
     */
    private void scheduleAffectedByRequiredResources(Stream<? extends ResourceKey> resources) {
        logger.trace("Scheduling tasks affected by required resources: " + resources);
        try(final StoreReadTxn txn = store.readTxn()) {
            resources.forEach((resource) -> BottomUpShared.directlyAffectedByRequiredResource(txn, resource, resourceService, logger, (key) -> {
                logger.trace("- scheduling: " + key);
                queue.add(key);
            }));
        }
    }

    /**
     * Schedules tasks affected by (changes to the) output of a task.
     */
    private void scheduleAffectedCallersOf(TaskKey callee, @Nullable Serializable output) {
        logger.trace("Scheduling tasks affected by output of: " + callee.toShortString(200));
        try(final StoreReadTxn txn = store.readTxn()) {
            // @formatter:off
            txn
                .callersOf(callee)
                .stream()
                .filter((caller) ->
                    // Skip callers that are not observable.
                    txn.taskObservability(caller).isObserved()
                    // Skip callers whose dependencies to the callee are consistent.
                 && txn.taskRequires(caller).stream().filter((dep) -> dep.calleeEqual(callee)).anyMatch((dep) -> !dep.isConsistent(output))
                )
                .forEach(key -> {
                    logger.trace("- scheduling: " + key);
                    queue.add(key);
                });
            // @formatter:on
        }
    }


    /**
     * Require the result of a task.
     */
    @Override
    public <O extends @Nullable Serializable> O require(TaskKey key, Task<O> task, boolean modifyObservability, CancelToken cancel) {
        cancel.throwIfCanceled();
        Stats.addRequires();
        layer.requireTopDownStart(key, task.input);
        executorLogger.requireTopDownStart(key, task);
        try {
            // Ignoring `modifyObservability` value, always assuming we want to modify observability in bottom-up builds.
            final TaskData data = getData(key, task, cancel);
            @SuppressWarnings({"unchecked"}) final O output = (O)data.output;
            executorLogger.requireTopDownEnd(key, task, output);
            return output;
        } finally {
            layer.requireTopDownEnd(key);
        }
    }

    /**
     * Get data for given task/key, either by getting existing data or through execution.
     */
    private TaskData getData(TaskKey key, Task<?> task, CancelToken cancel) {
        // Check if task was already visited this execution.
        final @Nullable TaskData visitedData = requireShared.dataFromVisited(key);
        if(visitedData != null) {
            // Validate required task against visited data.
            layer.validateVisited(key, task, visitedData);
            // If validation succeeds, return immediately.
            return visitedData;
        }

        // Check if data is stored for task. Execute if not.
        final @Nullable TaskData storedData = requireShared.dataFromStore(key);
        if(storedData == null) {
            // This task is new, therefore we execute it. We do not schedule tasks affected by this new task, since new
            // tasks cannot affect existing tasks.
            return exec(key, task, new NoData(), cancel);
        }

        // Task is in dependency graph, because we have stored data for it.

        if(storedData.taskObservability.isUnobserved()) {
            // Task is detached (not observed) and therefore may not be consistent because detached tasks are not
            // scheduled. Require the detached task it in a top-down manner to make it consistent.
            return requireUnobserved(key, task, storedData, cancel);
        }

        // The task is observed. It may be scheduled to be run, but we need its output *now*.
        final @Nullable TaskData requireNowData = requireScheduledNow(key, cancel);
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
                    return exec(key, task, reason, cancel);
                }
            }

            // Internal transient consistency output consistency.
            final @Nullable Serializable output = storedData.output;
            {
                final @Nullable InconsistentTransientOutput reason = requireShared.checkOutputConsistency(output);
                if(reason != null) {
                    return exec(key, task, reason, cancel);
                }
            }

            // Mark as visited.
            visited.put(key, storedData);

            // Invoke callback, if any.
            final @Nullable Consumer<@Nullable Serializable> callback = callbacks.get(key);
            if(callback != null) {
                executorLogger.invokeCallbackStart(callback, key, output);
                callback.accept(output);
                executorLogger.invokeCallbackEnd(callback, key, output);
            }
            return storedData;
        }
    }

    private TaskData requireUnobserved(TaskKey key, Task<?> task, TaskData storedData, CancelToken cancel) {
        // Input consistency.
        {
            final @Nullable InconsistentInput reason = requireShared.checkInput(storedData.input, task);
            if(reason != null) {
                return exec(key, task, reason, cancel);
            }
        }

        // Transient output consistency.
        {
            final @Nullable InconsistentTransientOutput reason =
                requireShared.checkOutputConsistency(storedData.output);
            if(reason != null) {
                return exec(key, task, reason, cancel);
            }
        }

        // Resource require consistency.
        for(ResourceRequireDep resourceRequireDep : storedData.resourceRequires) {
            final @Nullable InconsistentResourceRequire reason =
                requireShared.checkResourceRequireDep(key, task, resourceRequireDep);
            if(reason != null) {
                return exec(key, task, reason, cancel);
            }
        }

        // Resource provide consistency.
        for(ResourceProvideDep resourceProvideDep : storedData.resourceProvides) {
            final @Nullable InconsistentResourceProvide reason =
                requireShared.checkResourceProvideDep(key, task, resourceProvideDep);
            if(reason != null) {
                return exec(key, task, reason, cancel);
            }
        }

        // Task require consistency.
        for(TaskRequireDep taskRequireDep : storedData.taskRequires) {
            final @Nullable InconsistentTaskReq reason =
                requireShared.checkTaskRequireDep(key, task, taskRequireDep, this, true, cancel);
            if(reason != null) {
                return exec(key, task, reason, cancel);
            }
        }

        // Force observability status to observed in task data, so that validation and the visited map contain a consistent TaskData object.
        storedData = storedData.withTaskObservability(Observability.ImplicitObserved);

        // Validate well-formedness of the dependency graph, and set task to observed.
        try(final StoreWriteTxn txn = store.writeTxn()) {
            layer.validatePostWrite(key, storedData, txn);
            txn.setTaskObservability(key, Observability.ImplicitObserved);
        }

        // Mark task as visited.
        visited.put(key, storedData);

        // Invoke callback, if any.
        final @Nullable Consumer<@Nullable Serializable> callback = callbacks.get(key);
        if(callback != null) {
            executorLogger.invokeCallbackStart(callback, key, storedData.output);
            callback.accept(storedData.output);
            executorLogger.invokeCallbackEnd(callback, key, storedData.output);
        }

        return storedData;
    }

    /**
     * Execute the scheduled dependency of a task, and the task itself, which is required to be run *now*.
     */
    private @Nullable TaskData requireScheduledNow(TaskKey key, CancelToken cancel) {
        logger.trace("Executing scheduled (and its dependencies) task NOW: " + key);
        while(queue.isNotEmpty()) {
            cancel.throwIfCanceled();
            final @Nullable TaskKey minTaskKey = queue.pollLeastTaskWithDepTo(key, store);
            if(minTaskKey == null) {
                break;
            }
            final Task<?> minTask;
            try(final StoreReadTxn txn = store.readTxn()) {
                minTask = minTaskKey.toTask(taskDefs, txn);
            }
            logger.trace("- least element less than task: " + minTask.desc(100));
            final TaskData data = execAndSchedule(minTaskKey, minTask, cancel);
            if(minTaskKey.equals(key)) {
                return data; // Task was affected, and has been executed: return result.
            }
        }
        return null; // Task was not affected: return null.
    }


    public TaskData exec(TaskKey key, Task<?> task, ExecReason reason, CancelToken cancel) {
        return taskExecutor.exec(key, task, reason, this, true, cancel);
    }
}
