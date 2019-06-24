package mb.pie.runtime;

import mb.pie.api.ExecException;
import mb.pie.api.Observability;
import mb.pie.api.PieSession;
import mb.pie.api.Store;
import mb.pie.api.StoreReadTxn;
import mb.pie.api.StoreWriteTxn;
import mb.pie.api.Task;
import mb.pie.api.TaskDefs;
import mb.pie.api.TaskKey;
import mb.pie.api.exec.Cancelled;
import mb.pie.api.exec.NullCancelled;
import mb.pie.runtime.exec.BottomUpSession;
import mb.pie.runtime.exec.TopDownSession;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class PieSessionImpl implements PieSession {
    public final TopDownSession topDownSession;
    public final BottomUpSession bottomUpSession;

    private final TaskDefs taskDefs;
    private final Store store;
    private final ConcurrentHashMap<TaskKey, Consumer<@Nullable Serializable>> observers;


    public PieSessionImpl(
        TopDownSession topDownSession,
        BottomUpSession bottomUpSession,
        TaskDefs taskDefs,
        Store store,
        ConcurrentHashMap<TaskKey, Consumer<Serializable>> observers
    ) {
        this.topDownSession = topDownSession;
        this.bottomUpSession = bottomUpSession;
        this.taskDefs = taskDefs;
        this.store = store;
        this.observers = observers;
    }

    @Override public void close() {
        store.sync();
    }


    @Override
    public <O extends Serializable> O requireTopDown(Task<O> task) throws ExecException {
        try {
            return requireTopDown(task, new NullCancelled());
        } catch(InterruptedException e) {
            // Unexpected: NullCancelled is used, which does not check for interruptions.
            throw new RuntimeException("Unexpected InterruptedException", e);
        }
    }

    @Override
    public <O extends @Nullable Serializable> O requireTopDown(Task<O> task, Cancelled cancel) throws ExecException, InterruptedException {
        return topDownSession.requireInitial(task, cancel);
    }


    @Override
    public void requireBottomUp(Set<ResourceKey> changedResources) throws ExecException {
        try {
            requireBottomUp(changedResources, new NullCancelled());
        } catch(InterruptedException e) {
            // Unexpected: NullCancelled is used, which does not check for interruptions.
            throw new RuntimeException("Unexpected InterruptedException", e);
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
            // PERF: If more than 50% of required sources (resources that have no provider) have been changed (i.e.,
            // high-impact change), perform top-down builds for all observed tasks instead, since this has less overhead
            // than a bottom-up build.
            for(TaskKey key : observers.keySet()) { // TODO: get RootObserved tasks instead?
                try(final StoreReadTxn txn = store.readTxn()) {
                    final Task<?> task = key.toTask(taskDefs, txn);
                    topDownSession.requireInitial(task, cancel);
                }
            }
        } else {
            bottomUpSession.requireInitial(changedResources, cancel);
        }
    }


    @Override public void setUnobserved(TaskKey key) {
        try(StoreWriteTxn txn = store.writeTxn()) {
            Observability.explicitUnobserve(txn, key);
        }
    }

    @Override public void setUnobserved(Task<?> task) {
        setUnobserved(task.key());
    }
}
