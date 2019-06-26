package mb.pie.runtime;

import mb.pie.api.*;
import mb.pie.api.exec.Cancelled;
import mb.pie.api.exec.NullCancelled;
import mb.pie.runtime.exec.BottomUpSession;
import mb.pie.runtime.exec.TopDownSession;
import mb.resource.Resource;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class PieSessionImpl implements PieSession {
    // Public for testability of incrementality
    public final TopDownSession topDownSession;
    public final BottomUpSession bottomUpSession;

    private final TaskDefs taskDefs;
    private final ResourceService resourceService;
    private final Store store;
    private final ConcurrentHashMap<TaskKey, Consumer<@Nullable Serializable>> observers;


    public PieSessionImpl(
        TopDownSession topDownSession,
        BottomUpSession bottomUpSession,
        TaskDefs taskDefs,
        ResourceService resourceService, Store store,
        ConcurrentHashMap<TaskKey, Consumer<Serializable>> observers
    ) {
        this.topDownSession = topDownSession;
        this.bottomUpSession = bottomUpSession;
        this.taskDefs = taskDefs;
        this.resourceService = resourceService;
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


    @Override
    public void deleteUnobservedTasks(Function<Task<?>, Boolean> shouldDeleteTask, BiFunction<Task<?>, Resource, Boolean> shouldDeleteProvidedResource) throws IOException {
        try(StoreWriteTxn txn = store.writeTxn()) {
            // Start with tasks that have no callers: these are either ExplicitlyObserved, or Unobserved.
            final Deque<TaskKey> tasksToDelete = new ArrayDeque<>(txn.tasksWithoutCallers());
            while(!tasksToDelete.isEmpty()) {
                final TaskKey key = tasksToDelete.pop();
                if(!txn.taskObservability(key).isUnobserved()) {
                    // Do not delete observed tasks. This filters out ExplicitlyObserved tasks.
                    continue;
                }
                final Task<?> task = key.toTask(taskDefs, txn);
                if(!shouldDeleteTask.apply(task)) {
                    // Do not delete tasks that the caller of this function does not want to delete.
                    continue;
                }
                final @Nullable TaskData deletedData = txn.deleteData(key);
                if(deletedData != null) {
                    // Delete provided resources.
                    for(ResourceProvideDep dep : deletedData.resourceProvides) {
                        final Resource resource = resourceService.getResource(dep.key);
                        if(shouldDeleteProvidedResource.apply(task, resource)) {
                            if(resource instanceof HierarchicalResource) {
                                ((HierarchicalResource) resource).delete();
                            }
                        }
                    }

                    // Iterate the task requirements of the deleted task to continue deleting tasks.
                    deletedData.taskRequires
                        .stream()
                        // Filter out tasks that still have incoming callers.
                        .filter((d) -> txn.callersOf(d.callee).isEmpty())
                        // Push tasks onto the stack that have no incoming callers for deletion.
                        // The start of the while loop will ensure that only unobserved tasks will be deleted.
                        .forEach((d) -> tasksToDelete.push(d.callee));
                }
            }
        }
    }
}
