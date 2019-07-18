package mb.pie.runtime.exec;

import mb.pie.api.*;
import mb.pie.api.exec.Cancelled;
import mb.pie.api.exec.ExecReason;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BottomUpSession implements RequireTask {
    private final TaskDefs taskDefs;
    private final ResourceService resourceService;
    private final Store store;
    private final Layer layer;
    private final Logger logger;
    private final ExecutorLogger executorLogger;
    private final TaskExecutor taskExecutor;
    private final RequireShared requireShared;
    private final ConcurrentHashMap<TaskKey, Consumer<@Nullable Serializable>> callbacks;

    private final HashMap<TaskKey, TaskData> visited;
    private final DistinctTaskKeyPriorityQueue queue;

    public BottomUpSession(
        TaskDefs taskDefs,
        ResourceService resourceService,
        Store store,
        Layer layer,
        Logger logger,
        ExecutorLogger executorLogger,
        TaskExecutor taskExecutor,
        RequireShared requireShared,
        ConcurrentHashMap<TaskKey, Consumer<@Nullable Serializable>> callbacks,
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


    public void requireInitial(Set<ResourceKey> changedResources, Cancelled cancel) throws ExecException, InterruptedException {
        executorLogger.requireBottomUpInitialStart(changedResources);
        scheduleAffectedByResources(changedResources);
        execScheduled(cancel);
        executorLogger.requireBottomUpInitialEnd();
    }


    /**
     * Executes scheduled tasks (and schedules affected tasks) until queue is empty.
     */
    private void execScheduled(Cancelled cancel) throws ExecException, InterruptedException {
        logger.trace("Executing scheduled tasks: " + queue);
        while(queue.isNotEmpty()) {
            cancel.throwIfCancelled();
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
    private TaskData execAndSchedule(TaskKey key, Task<?> task, Cancelled cancel) throws ExecException, InterruptedException {
        final TaskData data = exec(key, task, new AffectedExecReason(), cancel);
        scheduleAffectedCallersOf(key, data.output);
        scheduleAffectedByResources(
            data.resourceProvides.stream().map((d) -> d.key).collect(Collectors.toCollection(HashSet::new)));
        return data;
    }

    /**
     * Schedules tasks affected by (changes to) files.
     */
    private void scheduleAffectedByResources(Collection<ResourceKey> resources) {
        logger.trace("Scheduling tasks affected by resources: " + resources);
        final HashSet<TaskKey> affected; // OPTO: avoid allocation of HashSet using stream/callback?
        try(final StoreReadTxn txn = store.readTxn()) {
            affected = BottomUpShared.directlyAffectedTaskKeys(txn, resources, resourceService, logger);
        }
        for(TaskKey key : affected) {
            logger.trace("- scheduling: " + key);
            queue.add(key);
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
    public <O extends @Nullable Serializable> O require(TaskKey key, Task<O> task, boolean modifyObservability, Cancelled cancel) throws ExecException, InterruptedException {
        cancel.throwIfCancelled();
        Stats.addRequires();
        layer.requireTopDownStart(key, task.input);
        executorLogger.requireTopDownStart(key, task);
        try {
            // Ignoring `modifyObservability` value, always assuming we want to modify observability in bottom-up builds.
            final TaskData data = getData(key, task, cancel);
            @SuppressWarnings("unchecked") final O output = (O) data.output;
            executorLogger.requireTopDownEnd(key, task, output);
            return output;
        } finally {
            layer.requireTopDownEnd(key);
        }
    }

    /**
     * Get data for given task/key, either by getting existing data or through execution.
     */
    private TaskData getData(TaskKey key, Task<?> task, Cancelled cancel) throws ExecException, InterruptedException {
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

    private TaskData requireUnobserved(TaskKey key, Task<?> task, TaskData storedData, Cancelled cancel) throws ExecException, InterruptedException {
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
    private @Nullable TaskData requireScheduledNow(TaskKey key, Cancelled cancel) throws ExecException, InterruptedException {
        logger.trace("Executing scheduled (and its dependencies) task NOW: " + key);
        while(queue.isNotEmpty()) {
            cancel.throwIfCancelled();
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


    public TaskData exec(TaskKey key, Task<?> task, ExecReason reason, Cancelled cancel) throws ExecException, InterruptedException {
        return taskExecutor.exec(key, task, reason, this, true, cancel);
    }
}
