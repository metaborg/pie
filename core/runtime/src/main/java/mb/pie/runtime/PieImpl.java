package mb.pie.runtime;

import mb.common.concurrent.lock.CloseableReentrantReadWriteLock;
import mb.common.concurrent.lock.LockHandle;
import mb.log.api.LoggerFactory;
import mb.pie.api.Callbacks;
import mb.pie.api.Layer;
import mb.pie.api.MixedSession;
import mb.pie.api.Pie;
import mb.pie.api.PieBuilder.LayerFactory;
import mb.pie.api.PieChildBuilder;
import mb.pie.api.Share;
import mb.pie.api.Store;
import mb.pie.api.StoreReadTxn;
import mb.pie.api.StoreWriteTxn;
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
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
    protected final CloseableReentrantReadWriteLock lock;


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
        Callbacks callbacks,
        CloseableReentrantReadWriteLock lock
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
        this.lock = lock;
    }

    @Override public void close() {
        if(isBase) {
            store.close();
        }
    }


    @Override public MixedSession newSession() {
        return createSession(lock.lockWrite());
    }

    @Override public Optional<MixedSession> tryNewSession() {
        return lock.tryLockWrite().map(this::createSession);
    }

    private MixedSessionImpl createSession(LockHandle lockHandle) {
        final Layer layer = layerFactory.apply(taskDefs, loggerFactory, serde);
        final Tracer tracer = tracerFactory.apply(loggerFactory);
        final HashMap<TaskKey, TaskData> visited = new HashMap<>();
        final HashSet<ResourceKey> providedResources = new HashSet<>();
        final TaskExecutor taskExecutor = new TaskExecutor(taskDefs, resourceService, share, defaultStampers, layer, loggerFactory, tracer, callbacks, visited, providedResources);
        final RequireShared requireShared = new RequireShared(taskDefs, resourceService, tracer, visited);
        final TopDownRunner topDownRunner = new TopDownRunner(store, layer, tracer, taskExecutor, requireShared, callbacks, visited);
        final BottomUpRunner bottomUpRunner = new BottomUpRunner(taskDefs, resourceService, store, layer, tracer, taskExecutor, requireShared, callbacks, visited);
        return new MixedSessionImpl(topDownRunner, bottomUpRunner, taskDefs, resourceService, store, tracer, callbacks, providedResources, lockHandle);
    }


    @Override public boolean hasBeenExecuted(TaskKey key) {
        try(final LockHandle ignored = lock.lockRead(); final StoreReadTxn txn = store.readTxn()) {
            return txn.getOutput(key) != null;
        }
    }


    @Override public void dropStore() {
        try(final LockHandle ignored = lock.lockWrite(); final StoreWriteTxn txn = store.writeTxn()) {
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
