package mb.pie.runtime;

import mb.log.api.LoggerFactory;
import mb.pie.api.Callbacks;
import mb.pie.api.Layer;
import mb.pie.api.MixedSession;
import mb.pie.api.Observability;
import mb.pie.api.Pie;
import mb.pie.api.PieBuilder.LayerFactory;
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
import mb.pie.api.serde.Serde;
import mb.pie.runtime.exec.BottomUpRunner;
import mb.pie.runtime.exec.RequireShared;
import mb.pie.runtime.exec.TaskExecutor;
import mb.pie.runtime.exec.TopDownRunner;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.function.Function;

public class PieImpl implements Pie {
    protected final boolean isBase;
    protected final TaskDefs taskDefs;
    protected final ResourceService resourceService;
    protected final Serde serde;
    protected final Store store;
    protected final Share share;
    protected final DefaultStampers defaultStampers;
    protected final LayerFactory layerFactory;
    protected final LoggerFactory loggerFactory;
    protected final Function<LoggerFactory, Tracer> tracerFactory;
    protected final Callbacks callbacks;


    public PieImpl(
        boolean ownsStore,
        TaskDefs taskDefs,
        ResourceService resourceService,
        Serde serde,
        Store store,
        Share share,
        DefaultStampers defaultStampers,
        LayerFactory layerFactory,
        LoggerFactory loggerFactory,
        Function<LoggerFactory, Tracer> tracerFactory,
        Callbacks callbacks
    ) {
        this.isBase = ownsStore;
        this.taskDefs = taskDefs;
        this.resourceService = resourceService;
        this.serde = serde;
        this.store = store;
        this.share = share;
        this.defaultStampers = defaultStampers;
        this.layerFactory = layerFactory;
        this.loggerFactory = loggerFactory;
        this.tracerFactory = tracerFactory;
        this.callbacks = callbacks;
    }

    @Override public void close() {
        if(isBase) {
            store.close();
        }
    }


    @Override public MixedSession newSession() {
        final Layer layer = layerFactory.apply(taskDefs, loggerFactory, serde);
        final Tracer tracer = tracerFactory.apply(loggerFactory);
        final HashMap<TaskKey, TaskData> visited = new HashMap<>();
        final HashSet<ResourceKey> providedResources = new HashSet<>();
        final TaskExecutor taskExecutor = new TaskExecutor(taskDefs, resourceService, share, defaultStampers, layer, loggerFactory, tracer, callbacks, visited, providedResources);
        final RequireShared requireShared = new RequireShared(taskDefs, resourceService, tracer, visited);
        final TopDownRunner topDownRunner = new TopDownRunner(store, layer, tracer, taskExecutor, requireShared, callbacks, visited);
        final BottomUpRunner bottomUpRunner = new BottomUpRunner(taskDefs, resourceService, store, layer, tracer, taskExecutor, requireShared, callbacks, visited);
        return new MixedSessionImpl(topDownRunner, bottomUpRunner, taskDefs, resourceService, store, tracer, providedResources);
    }


    @Override public boolean hasBeenExecuted(TaskKey key) {
        try(final StoreReadTxn txn = store.readTxn()) {
            return txn.getOutput(key) != null;
        }
    }

    @Override public boolean isObserved(TaskKey key) {
        try(final StoreReadTxn txn = store.readTxn()) {
            return txn.getTaskObservability(key).isObserved();
        }
    }

    @Override public boolean isExplicitlyObserved(TaskKey key) {
        try(final StoreReadTxn txn = store.readTxn()) {
            return txn.getTaskObservability(key) == Observability.ExplicitObserved;
        }
    }

    @Override public void setImplicitToExplicitlyObserved(TaskKey key) {
        try(final StoreWriteTxn txn = store.writeTxn()) {
            final Observability observability = txn.getTaskObservability(key);
            if(observability.isUnobserved()) {
                throw new IllegalArgumentException("Cannot set task with key '" + key + "' to explicitly observed, because it is unobserved");
            }
            if(observability != Observability.ExplicitObserved) {
                txn.setTaskObservability(key, Observability.ExplicitObserved);
            }
        }
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

    @Override public void addToChildBuilder(PieChildBuilder childBuilder) {
        childBuilder.addTaskDefs(taskDefs);
        childBuilder.addResourceService(resourceService);
        childBuilder.addCallBacks(callbacks);
    }

    @Override public String toString() {
        return "PieImpl(" + store + ", " + share + ", " + defaultStampers + ", " + layerFactory.apply(taskDefs, loggerFactory, serde) + ")";
    }
}
