package mb.pie.runtime;

import mb.log.api.LoggerFactory;
import mb.pie.api.Callbacks;
import mb.pie.api.Layer;
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
import mb.pie.api.Tracer;
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
    protected final BiFunction<TaskDefs, LoggerFactory, Layer> layerFactory;
    protected final LoggerFactory loggerFactory;
    protected final Function<LoggerFactory, Tracer> tracerFactory;
    protected final Callbacks callbacks;


    public PieImpl(
        TaskDefs taskDefs,
        ResourceService resourceService,
        Store store,
        Share share,
        DefaultStampers defaultStampers,
        BiFunction<TaskDefs, LoggerFactory, Layer> layerFactory,
        LoggerFactory loggerFactory,
        Function<LoggerFactory, Tracer> tracerFactory,
        Callbacks callbacks
    ) {
        this.taskDefs = taskDefs;
        this.resourceService = resourceService;
        this.store = store;
        this.share = share;
        this.defaultStampers = defaultStampers;
        this.layerFactory = layerFactory;
        this.loggerFactory = loggerFactory;
        this.tracerFactory = tracerFactory;
        this.callbacks = callbacks;
    }

    @Override public void close() {
        store.close();
    }


    @Override public MixedSession newSession() {
        final Layer layer = layerFactory.apply(taskDefs, loggerFactory);
        final Tracer tracer = tracerFactory.apply(loggerFactory);
        final HashMap<TaskKey, TaskData> visited = new HashMap<>();
        final TaskExecutor taskExecutor = new TaskExecutor(taskDefs, resourceService, share, defaultStampers, layer, loggerFactory, tracer, callbacks, visited);
        final RequireShared requireShared = new RequireShared(taskDefs, resourceService, tracer, visited);
        final TopDownRunner topDownRunner = new TopDownRunner(store, layer, tracer, taskExecutor, requireShared, callbacks, visited);
        final BottomUpRunner bottomUpRunner = new BottomUpRunner(taskDefs, resourceService, store, layer, tracer, taskExecutor, requireShared, callbacks, visited);
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
        for(Pie ancestor : ancestors) {
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

    @Override public String toString() {
        return "PieImpl(" + store + ", " + share + ", " + defaultStampers + ", " + layerFactory.apply(taskDefs, loggerFactory) + ")";
    }
}
