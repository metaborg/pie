package mb.pie.runtime.exec;

import mb.pie.api.*;
import mb.pie.api.exec.Cancelled;
import mb.pie.api.exec.ExecReason;
import mb.pie.api.fs.FileSystemResource;
import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamper;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BottomUpSession implements RequireTask {
    private final TaskDefs taskDefs;
    private final ResourceSystems resourceSystems;
    private final Map<TaskKey, Consumer<@Nullable Serializable>> observers;
    private final Store store;
    private final Layer layer;
    private final Logger logger;
    private final ExecutorLogger executorLogger;

    private final HashMap<TaskKey, TaskData<?, ?>> visited = new HashMap<>();
    private final DistinctTaskKeyPriorityQueue queue;
    private final TaskExecutor executor;
    private final RequireShared requireShared;

    public BottomUpSession(
        TaskDefs taskDefs,
        ResourceSystems resourceSystems,
        Map<TaskKey, Consumer<@Nullable Serializable>> observers,
        Store store,
        Share share,
        OutputStamper defaultOutputStamper,
        ResourceStamper<FileSystemResource> defaultRequireFileSystemStamper,
        ResourceStamper<FileSystemResource> defaultProvideFileSystemStamper,
        Layer layer,
        Logger logger,
        ExecutorLogger executorLogger
    ) {
        this.taskDefs = taskDefs;
        this.resourceSystems = resourceSystems;
        this.observers = observers;
        this.store = store;
        this.layer = layer;
        this.logger = logger;
        this.executorLogger = executorLogger;

        this.queue = DistinctTaskKeyPriorityQueue.withTransitiveDependencyComparator(store);
        this.executor = new TaskExecutor(taskDefs, resourceSystems, visited, store, share, defaultOutputStamper, defaultRequireFileSystemStamper, defaultProvideFileSystemStamper, layer, logger, executorLogger, (TaskKey key, TaskData<?, ?> data) -> {
            // Notify observer, if any.
            final @Nullable Consumer<@Nullable Serializable> observer = observers.get(key);
            if(observer != null) {
                final @Nullable Serializable output = data.output;
                executorLogger.invokeObserverStart(observer, key, output);
                observer.accept(output);
                executorLogger.invokeObserverEnd(observer, key, output);
            }
        });
        this.requireShared = new RequireShared(taskDefs, resourceSystems, visited, store, executorLogger);
    }


    /**
     * Entry point for top-down builds.
     */
    public <I extends Serializable, O extends @Nullable Serializable> O requireTopDownInitial(Task<I, O> task, Cancelled cancel) throws ExecException, InterruptedException {
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

    /**
     * Entry point for bottom-up builds.
     */
    public void requireBottomUpInitial(Set<ResourceKey> changedResources, Cancelled cancel) throws ExecException, InterruptedException {
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
            final Task<Serializable, @Nullable Serializable> task;
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
    private <I extends Serializable, O extends @Nullable Serializable> TaskData<I, O> execAndSchedule(TaskKey key, Task<I, O> task, Cancelled cancel) throws ExecException, InterruptedException {
        final TaskData<I, O> data = exec(key, task, new AffectedExecReason(), cancel);
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
            affected = BottomUpShared.directlyAffectedTaskKeys(txn, resources, resourceSystems, logger);
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
                .filter((caller) -> txn.taskRequires(caller).stream().filter((dep) -> dep.calleeEqual(callee)).anyMatch((dep) -> !dep.isConsistent(output)))
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
    public <I extends Serializable, O extends @Nullable Serializable> O require(TaskKey key, Task<I, O> task, Cancelled cancel) throws ExecException, InterruptedException {
        Stats.addRequires();
        cancel.throwIfCancelled();
        layer.requireTopDownStart(key, task.input);
        executorLogger.requireTopDownStart(key, task);
        try {
            final TaskData<I, O> data = getData(key, task, cancel);
            final O output = data.output;
            executorLogger.requireTopDownEnd(key, task, output);
            return output;
        } finally {
            layer.requireTopDownEnd(key);
        }
    }

    /**
     * Get data for given task/key, either by getting existing data or through execution.
     */
    private <I extends Serializable, O extends @Nullable Serializable> TaskData<I, O> getData(TaskKey key, Task<I, O> task, Cancelled cancel) throws ExecException, InterruptedException {
        // Check if task was already visited this execution. Return immediately if so.
        final @Nullable TaskData<?, ?> visitedData = requireShared.dataFromVisited(key);
        if(visitedData != null) {
            return visitedData.cast();
        }

        // Check if data is stored for task. Execute if not.
        final @Nullable TaskData<?, ?> storedData = requireShared.dataFromStore(key);
        if(storedData == null) {
            // This tasks's output cannot affect other tasks since it is new. Therefore, we do not have to schedule new tasks.
            return exec(key, task, new NoData(), cancel);
        }

        // Task is : dependency graph. It may be scheduled to be run, but we need its output *now*.
        final @Nullable TaskData<?, ?> requireNowData = requireScheduledNow(key, cancel);
        if(requireNowData != null) {
            // Task was scheduled. That is, it was either directly or indirectly affected. Therefore, it has been executed.
            return requireNowData.cast();
        } else {
            // Task was not scheduled. That is, it was not directly affected by file changes, and not indirectly affected by other tasks.
            // Therefore, it has not been executed. However, the task may still be affected by internal inconsistencies.
            final TaskData<I, O> existingData = storedData.cast();
            final I input = existingData.input;
            final O output = existingData.output;

            // Internal input consistency changes.
            {
                final @Nullable InconsistentInput reason = requireShared.checkInput(input, task);
                if(reason != null) {
                    return exec(key, task, reason, cancel);
                }
            }

            // Internal transient consistency output consistency.
            {
                final @Nullable InconsistentTransientOutput reason = requireShared.checkOutputConsistency(output);
                if(reason != null) {
                    return exec(key, task, reason, cancel);
                }
            }

            // Notify observer.
            final @Nullable Consumer<@Nullable Serializable> observer = observers.get(key);
            if(observer != null) {
                executorLogger.invokeObserverStart(observer, key, output);
                observer.accept(output);
                executorLogger.invokeObserverEnd(observer, key, output);
            }

            // Task is consistent.
            return existingData;
        }
    }

    /**
     * Execute the scheduled dependency of a task, and the task itself, which is required to be run *now*.
     */
    private @Nullable TaskData<?, ?> requireScheduledNow(TaskKey key, Cancelled cancel) throws ExecException, InterruptedException {
        logger.trace("Executing scheduled (and its dependencies) task NOW: " + key);
        while(queue.isNotEmpty()) {
            cancel.throwIfCancelled();
            final @Nullable TaskKey minTaskKey = queue.pollLeastTaskWithDepTo(key, store);
            if(minTaskKey == null) {
                break;
            }
            final Task<Serializable, @Nullable Serializable> minTask;
            try(final StoreReadTxn txn = store.readTxn()) {
                minTask = minTaskKey.toTask(taskDefs, txn);
            }
            logger.trace("- least element less than task: " + minTask.desc(100));
            final TaskData<Serializable, @Nullable Serializable> data = execAndSchedule(minTaskKey, minTask, cancel);
            if(minTaskKey.equals(key)) {
                return data; // Task was affected, and has been return executed result
            }
        }
        return null; // Task was not return affected null
    }


    public <I extends Serializable, O extends @Nullable Serializable> TaskData<I, O> exec(TaskKey key, Task<I, O> task, ExecReason reason, Cancelled cancel) throws ExecException, InterruptedException {
        return executor.exec(key, task, reason, this, cancel);
    }
}
