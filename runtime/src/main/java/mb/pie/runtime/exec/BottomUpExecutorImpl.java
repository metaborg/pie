package mb.pie.runtime.exec;

import mb.pie.api.*;
import mb.pie.api.exec.BottomUpExecutor;
import mb.pie.api.exec.Cancelled;
import mb.pie.api.exec.NullCancelled;
import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamper;
import mb.pie.runtime.DefaultStampers;
import mb.resource.ResourceKey;
import mb.resource.ResourceRegistry;
import mb.resource.fs.FSResource;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class BottomUpExecutorImpl implements BottomUpExecutor {
    private final TaskDefs taskDefs;
    private final ResourceRegistry resourceRegistry;
    private final Store store;
    private final Share share;
    private final DefaultStampers defaultStampers;
    private final Function<Logger, Layer> layerFactory;
    private final Logger logger;
    private final Function<Logger, ExecutorLogger> executorLoggerFactory;
    private final ConcurrentHashMap<TaskKey, Consumer<@Nullable Serializable>> observers = new ConcurrentHashMap<>();

    public BottomUpExecutorImpl(
        TaskDefs taskDefs,
        ResourceRegistry resourceRegistry,
        Store store,
        Share share,
        DefaultStampers defaultStampers,
        Function<Logger, Layer> layerFactory,
        Logger logger,
        Function<Logger, ExecutorLogger> executorLoggerFactory
    ) {
        this.taskDefs = taskDefs;
        this.resourceRegistry = resourceRegistry;
        this.store = store;
        this.share = share;
        this.defaultStampers = defaultStampers;
        this.layerFactory = layerFactory;
        this.logger = logger;
        this.executorLoggerFactory = executorLoggerFactory;
    }


    @Override
    public <I extends Serializable, O extends @Nullable Serializable> O requireTopDown(Task<I, O> task) throws ExecException {
        try {
            return requireTopDown(task, new NullCancelled());
        } catch(InterruptedException e) {
            // Cannot occur: NullCancelled is used, which does not check for interruptions.
            throw new RuntimeException(e);
        }
    }

    @Override
    public <I extends Serializable, O extends @Nullable Serializable> O requireTopDown(Task<I, O> task, Cancelled cancel) throws ExecException, InterruptedException {
        final BottomUpSession session = newSession();
        return session.requireTopDownInitial(task, cancel);
    }

    @Override
    public void requireBottomUp(Set<ResourceKey> changedResources) throws ExecException {
        try {
            requireBottomUp(changedResources, new NullCancelled());
        } catch(InterruptedException e) {
            // Cannot occur: NullCancelled is used, which does not check for interruptions.
        }
    }

    @Override
    public void requireBottomUp(Set<ResourceKey> changedResources, Cancelled cancel) throws ExecException, InterruptedException {
        if(changedResources.isEmpty()) return;

        final float numSourceFiles;
        try(final StoreReadTxn txn = store.readTxn()) {
            numSourceFiles = txn.numSourceFiles();
        }

        final float changedRate = (float) changedResources.size() / numSourceFiles;
        if(changedRate > 0.5) {
            final TopDownSessionImpl topdownSession =
                new TopDownSessionImpl(taskDefs, resourceRegistry, store, share, defaultStampers, layerFactory.apply(logger),
                    logger, executorLoggerFactory.apply(logger));
            for(TaskKey key : observers.keySet()) {
                try(final StoreReadTxn txn = store.readTxn()) {
                    final Task<Serializable, @Nullable Serializable> task = key.toTask(taskDefs, txn);
                    topdownSession.requireInitial(task, cancel);
                    // TODO: observers are not called when using a topdown session.
                }
            }
        } else {
            final BottomUpSession session = newSession();
            session.requireBottomUpInitial(changedResources, cancel);
        }
    }

    @Override
    public boolean hasBeenRequired(TaskKey key) {
        try(final StoreReadTxn txn = store.readTxn()) {
            return txn.output(key) != null;
        }
    }

    @Override
    public void setObserver(TaskKey key, Consumer<@Nullable Serializable> observer) {
        observers.put(key, observer);
    }

    @Override
    public void removeObserver(TaskKey key) {
        observers.remove(key);
    }

    @Override
    public void dropObservers() {
        observers.clear();
    }


    public BottomUpSession newSession() {
        return new BottomUpSession(taskDefs, resourceRegistry, observers, store, share, defaultStampers, layerFactory.apply(logger), logger,
            executorLoggerFactory.apply(logger));
    }
}

