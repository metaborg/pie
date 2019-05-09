package mb.pie.runtime.exec;

import mb.pie.api.*;
import mb.pie.api.exec.Cancelled;
import mb.pie.api.exec.ExecReason;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
    private final ConcurrentHashMap<TaskKey, Consumer<@Nullable Serializable>> observers;

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
        ConcurrentHashMap<TaskKey, Consumer<@Nullable Serializable>> observers,
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
        this.observers = observers;

        this.visited = visited;
        this.queue = DistinctTaskKeyPriorityQueue.withTransitiveDependencyComparator(store);
    }


    public void requireInitial(Set<ResourceKey> changedResources, Cancelled cancel) throws ExecException, InterruptedException {
        try {
            executorLogger.requireBottomUpInitialStart(changedResources);
            scheduleAffectedByResources(changedResources);
            execScheduled(cancel);
            executorLogger.requireBottomUpInitialEnd();
        } finally {
            store.sync();
        }
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
        return data;
    }

    /**
     * Schedules tasks affected by (changes to) files.
     */
    private void scheduleAffectedByResources(Set<ResourceKey> resources) {
        logger.trace("Scheduling tasks affected by resources: " + resources);
        final HashSet<TaskKey> affected;
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
            final List<TaskKey> inconsistentCallers = txn
                .callersOf(callee)
                .stream()
                .filter((caller) -> txn.taskRequires(caller).stream().filter((dep) -> dep.calleeEqual(callee)).anyMatch(
                    (dep) -> !dep.isConsistent(output)))
                .collect(Collectors.toList());

            for(TaskKey key : inconsistentCallers) {
                logger.trace("- scheduling: " + key);
                queue.add(key);
            }
        }
    }


    /**
     * Require the result of a task.
     */
    @Override
    public <O extends @Nullable Serializable> O require(TaskKey key, Task<O> task, Cancelled cancel) throws ExecException, InterruptedException {
        cancel.throwIfCancelled();
        Stats.addRequires();
        layer.requireTopDownStart(key, task.input);
        executorLogger.requireTopDownStart(key, task);
        try {
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
        // Check if task was already visited this execution. Return immediately if so.
        final @Nullable TaskData visitedData = requireShared.dataFromVisited(key);
        if(visitedData != null) {
            return visitedData;
        }

        // Check if data is stored for task. Execute if not.
        final @Nullable TaskData storedData = requireShared.dataFromStore(key);
        if(storedData == null) {
            // This tasks's output cannot affect other tasks since it is new. Therefore, we do not have to schedule new tasks.
            return exec(key, task, new NoData(), cancel);
        }

        // Task is in dependency graph. It may be scheduled to be run, but we need its output *now*.
        final @Nullable TaskData requireNowData = requireScheduledNow(key, cancel);
        if(requireNowData != null) {
            // Task was scheduled. That is, it was either directly or indirectly affected. Therefore, it has been executed.
            return requireNowData;
        } else {
            // Task was not scheduled. That is, it was not directly affected by file changes, and not indirectly affected by other tasks.
            // Therefore, it has not been executed. However, the task may still be affected by internal inconsistencies.
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
            // Notify observer, if any.
            final @Nullable Consumer<@Nullable Serializable> observer = observers.get(key);
            if(observer != null) {
                executorLogger.invokeObserverStart(observer, key, output);
                observer.accept(output);
                executorLogger.invokeObserverEnd(observer, key, output);
            }
            return storedData;
        }
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
        return taskExecutor.exec(key, task, reason, this, cancel);
    }
}
