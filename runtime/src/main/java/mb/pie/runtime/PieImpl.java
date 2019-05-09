package mb.pie.runtime;

import mb.pie.api.*;
import mb.pie.runtime.exec.BottomUpSession;
import mb.pie.runtime.exec.RequireShared;
import mb.pie.runtime.exec.TaskExecutor;
import mb.pie.runtime.exec.TopDownSession;
import mb.pie.runtime.taskdefs.CompositeTaskDefs;
import mb.resource.ResourceService;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class PieImpl implements Pie {
    private final TaskDefs taskDefs;
    private final ResourceService resourceService;
    private final Store store;
    private final Share share;
    private final DefaultStampers defaultStampers;
    private final BiFunction<TaskDefs, Logger, Layer> layerFactory;
    private final Logger logger;
    private final Function<Logger, ExecutorLogger> executorLoggerFactory;

    private final ConcurrentHashMap<TaskKey, Consumer<@Nullable Serializable>> observers = new ConcurrentHashMap<>();

    public PieImpl(
        TaskDefs taskDefs,
        ResourceService resourceService,
        Store store,
        Share share,
        DefaultStampers defaultStampers,
        BiFunction<TaskDefs, Logger, Layer> layerFactory,
        Logger logger,
        Function<Logger, ExecutorLogger> executorLoggerFactory
    ) {
        this.taskDefs = taskDefs;
        this.resourceService = resourceService;
        this.store = store;
        this.share = share;
        this.defaultStampers = defaultStampers;
        this.layerFactory = layerFactory;
        this.logger = logger;
        this.executorLoggerFactory = executorLoggerFactory;
    }

    @Override public void close() {
        store.close();
    }


    @Override public PieSession newSession() {
        return createSession(this.taskDefs);
    }

    @Override public PieSession newSession(TaskDefs addTaskDefs) {
        return createSession(new CompositeTaskDefs(this.taskDefs, addTaskDefs));
    }

    private PieSession createSession(TaskDefs taskDefs) {
        final Layer layer = layerFactory.apply(taskDefs, logger);
        final ExecutorLogger executorLogger = executorLoggerFactory.apply(logger);
        final HashMap<TaskKey, TaskData> visited = new HashMap<>();
        final TaskExecutor taskExecutor =
            new TaskExecutor(taskDefs, resourceService, store, share, defaultStampers, layer, logger, executorLogger,
                observers, visited);
        final RequireShared requireShared =
            new RequireShared(taskDefs, resourceService, store, executorLogger, visited);
        final TopDownSession topDownSession =
            new TopDownSession(store, layer, executorLogger, taskExecutor, requireShared, observers, visited);
        final BottomUpSession bottomUpSession =
            new BottomUpSession(taskDefs, resourceService, store, layer, logger, executorLogger, taskExecutor,
                requireShared, observers, visited);
        return new PieSessionImpl(topDownSession, bottomUpSession, taskDefs, store, observers);
    }


    @Override public boolean hasBeenExecuted(TaskKey key) {
        try(final StoreReadTxn txn = store.readTxn()) {
            return txn.output(key) != null;
        }
    }

    @Override public boolean hasBeenExecuted(Task<?> task) {
        return hasBeenExecuted(task.key());
    }

    @Override public <O extends @Nullable Serializable> void setObserver(Task<O> task, Consumer<O> observer) {
        @SuppressWarnings("unchecked") final Consumer<@Nullable Serializable> generalizedObserver =
            (Consumer<@Nullable Serializable>) observer;
        observers.put(task.key(), generalizedObserver);
    }

    @Override public void setObserver(TaskKey key, Consumer<@Nullable Serializable> observer) {
        observers.put(key, observer);
    }

    @Override public void removeObserver(Task<?> task) {
        observers.remove(task.key());
    }

    @Override public void removeObserver(TaskKey key) {
        observers.remove(key);
    }

    @Override public void dropObservers() {
        observers.clear();
    }


    @Override public void dropStore() {
        try(final StoreWriteTxn txn = store.writeTxn()) {
            txn.drop();
        }
    }


    @Override public String toString() {
        return "PieImpl(" + store + ", " + share + ", " + defaultStampers + ", " + layerFactory.apply(taskDefs, logger) + ")";
    }
}
