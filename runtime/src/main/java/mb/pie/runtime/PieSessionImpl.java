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
    protected final TopDownSession topDownSession;
    protected final BottomUpSession bottomUpSession;

    protected final TaskDefs taskDefs;
    protected final ResourceService resourceService;
    protected final Store store;
    protected final ConcurrentHashMap<TaskKey, Consumer<@Nullable Serializable>> callbacks;


    public PieSessionImpl(
        TopDownSession topDownSession,
        BottomUpSession bottomUpSession,
        TaskDefs taskDefs,
        ResourceService resourceService, Store store,
        ConcurrentHashMap<TaskKey, Consumer<Serializable>> callbacks
    ) {
        this.topDownSession = topDownSession;
        this.bottomUpSession = bottomUpSession;
        this.taskDefs = taskDefs;
        this.resourceService = resourceService;
        this.store = store;
        this.callbacks = callbacks;
    }

    @Override public void close() {
        store.sync();
    }


    @Override public <O extends Serializable> O require(Task<O> task) throws ExecException {
        try {
            return require(task, new NullCancelled());
        } catch(InterruptedException e) {
            // Unexpected: NullCancelled is used, which does not check for interruptions.
            throw new RuntimeException("Unexpected InterruptedException", e);
        }
    }

    @Override
    public <O extends @Nullable Serializable> O require(Task<O> task, Cancelled cancel) throws ExecException, InterruptedException {
        return topDownSession.requireInitial(task, false, cancel);
    }


    @Override public <O extends Serializable> O requireAndObserve(Task<O> task) throws ExecException {
        try {
            return requireAndObserve(task, new NullCancelled());
        } catch(InterruptedException e) {
            // Unexpected: NullCancelled is used, which does not check for interruptions.
            throw new RuntimeException("Unexpected InterruptedException", e);
        }
    }

    @Override
    public <O extends @Nullable Serializable> O requireAndObserve(Task<O> task, Cancelled cancel) throws ExecException, InterruptedException {
        return topDownSession.requireInitial(task, true, cancel);
    }


    @Override
    public void updateAffectedBy(Set<ResourceKey> changedResources) throws ExecException {
        try {
            updateAffectedBy(changedResources, new NullCancelled());
        } catch(InterruptedException e) {
            // Unexpected: NullCancelled is used, which does not check for interruptions.
            throw new RuntimeException("Unexpected InterruptedException", e);
        }
    }

    @Override
    public void updateAffectedBy(Set<ResourceKey> changedResources, Cancelled cancel) throws ExecException, InterruptedException {
        if(changedResources.isEmpty()) return;
        bottomUpSession.requireInitial(changedResources, cancel);
    }


    @Override public void unobserve(TaskKey key) {
        try(StoreWriteTxn txn = store.writeTxn()) {
            Observability.explicitUnobserve(txn, key);
        }
    }

    @Override public void unobserve(Task<?> task) {
        unobserve(task.key());
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
