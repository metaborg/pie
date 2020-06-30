package mb.pie.runtime;

import mb.pie.api.Callbacks;
import mb.pie.api.ExecutorLogger;
import mb.pie.api.Layer;
import mb.pie.api.Logger;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.pie.api.PieChildBuilder;
import mb.pie.api.Share;
import mb.pie.api.Store;
import mb.pie.api.StoreReadTxn;
import mb.pie.api.StoreWriteTxn;
import mb.pie.api.Task;
import mb.pie.api.TaskData;
import mb.pie.api.TaskDefs;
import mb.pie.api.TaskKey;
import mb.pie.runtime.exec.BottomUpRunner;
import mb.pie.runtime.exec.RequireShared;
import mb.pie.runtime.exec.TaskExecutor;
import mb.pie.runtime.exec.TopDownRunner;
import mb.resource.ResourceService;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class PieImpl implements Pie {
    protected final TaskDefs taskDefs;
    protected final ResourceService resourceService;
    protected final Store store;
    protected final Share share;
    protected final DefaultStampers defaultStampers;
    protected final BiFunction<TaskDefs, Logger, Layer> layerFactory;
    protected final Logger logger;
    protected final Function<Logger, ExecutorLogger> executorLoggerFactory;
    protected final Callbacks callbacks;


    public PieImpl(
        TaskDefs taskDefs,
        ResourceService resourceService,
        Store store,
        Share share,
        DefaultStampers defaultStampers,
        BiFunction<TaskDefs, Logger, Layer> layerFactory,
        Logger logger,
        Function<Logger, ExecutorLogger> executorLoggerFactory,
        Callbacks callbacks
    ) {
        this.taskDefs = taskDefs;
        this.resourceService = resourceService;
        this.store = store;
        this.share = share;
        this.defaultStampers = defaultStampers;
        this.layerFactory = layerFactory;
        this.logger = logger;
        this.executorLoggerFactory = executorLoggerFactory;
        this.callbacks = callbacks;
    }

    @Override public void close() {
        store.close();
    }

    @Override public MixedSession newSession() {
        final Layer layer = layerFactory.apply(taskDefs, logger);
        final ExecutorLogger executorLogger = executorLoggerFactory.apply(logger);
        final HashMap<TaskKey, TaskData> visited = new HashMap<>();
        final TaskExecutor taskExecutor =
            new TaskExecutor(taskDefs, resourceService, store, share, defaultStampers, layer, logger, executorLogger,
                callbacks, visited);
        final RequireShared requireShared =
            new RequireShared(taskDefs, resourceService, store, executorLogger, visited);
        final TopDownRunner topDownRunner =
            new TopDownRunner(store, layer, executorLogger, taskExecutor, requireShared, callbacks, visited);
        final BottomUpRunner bottomUpRunner =
            new BottomUpRunner(taskDefs, resourceService, store, layer, logger, executorLogger, taskExecutor,
                requireShared, callbacks, visited);
        return new MixedSessionImpl(topDownRunner, bottomUpRunner, taskDefs, resourceService, store);
    }


    @Override public boolean hasBeenExecuted(TaskKey key) {
        try(final StoreReadTxn txn = store.readTxn()) {
            return txn.output(key) != null;
        }
    }

    @Override public boolean hasBeenExecuted(Task<?> task) {
        return hasBeenExecuted(task.key());
    }


    @Override public boolean isObserved(TaskKey key) {
        try(final StoreReadTxn txn = store.readTxn()) {
            return txn.taskObservability(key).isObserved();
        }
    }

    @Override public boolean isObserved(Task<?> task) {
        return isObserved(task.key());
    }


    @Override public <O extends @Nullable Serializable> void setCallback(Task<O> task, Consumer<O> function) {
        callbacks.set(task, function);
    }

    @Override public void setCallback(TaskKey key, Consumer<@Nullable Serializable> function) {
        callbacks.set(key, function);
    }

    @Override public void removeCallback(Task<?> task) {
        callbacks.remove(task);
    }

    @Override public void removeCallback(TaskKey key) {
        callbacks.remove(key);
    }

    @Override public void dropCallbacks() {
        callbacks.clear();
    }


    @Override public void dropStore() {
        try(final StoreWriteTxn txn = store.writeTxn()) {
            txn.drop();
        }
    }


    @Override public PieChildBuilder createChildBuilder(Pie... ancestors) {
        PieChildBuilderImpl builder = new PieChildBuilderImpl(this);
        for (Pie ancestor : ancestors) {
            ancestor.addToChildBuilder(builder);
        }
        return builder;
    }

    @Override
    public void addToChildBuilder(PieChildBuilder childBuilder) {
        childBuilder.addTaskDefs(taskDefs);
        childBuilder.addResourceService(resourceService);
        childBuilder.addCallBacks(callbacks);
    }


    public TaskDefs getTaskDefs() {
        return taskDefs;
    }

    public Callbacks getCallbacks() {
        return callbacks;
    }

    public ResourceService getResourceService() {
        return resourceService;
    }

    @Override public String toString() {
        return "PieImpl(" + store + ", " + share + ", " + defaultStampers + ", " + layerFactory.apply(taskDefs, logger) + ")";
    }
}
