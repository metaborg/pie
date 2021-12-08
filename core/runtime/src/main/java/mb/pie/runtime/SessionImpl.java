package mb.pie.runtime;

import mb.pie.api.Callbacks;
import mb.pie.api.ExecException;
import mb.pie.api.Observability;
import mb.pie.api.ResourceProvideDep;
import mb.pie.api.SerializableConsumer;
import mb.pie.api.Session;
import mb.pie.api.Store;
import mb.pie.api.StoreReadTxn;
import mb.pie.api.StoreWriteTxn;
import mb.pie.api.Task;
import mb.pie.api.TaskData;
import mb.pie.api.TaskDefs;
import mb.pie.api.TaskKey;
import mb.pie.api.Tracer;
import mb.pie.api.UncheckedExecException;
import mb.pie.api.exec.CanceledException;
import mb.pie.api.exec.UncheckedInterruptedException;
import mb.resource.Resource;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class SessionImpl implements Session {
    protected final TaskDefs taskDefs;
    protected final ResourceService resourceService;
    protected final Store store;
    protected final Tracer tracer;
    protected final Callbacks callbacks;

    protected final HashSet<ResourceKey> providedResources;


    public SessionImpl(
        TaskDefs taskDefs,
        ResourceService resourceService,
        Store store,
        Tracer tracer,
        Callbacks callbacks,
        HashSet<ResourceKey> providedResources
    ) {
        this.taskDefs = taskDefs;
        this.resourceService = resourceService;
        this.store = store;
        this.tracer = tracer;
        this.callbacks = callbacks;
        this.providedResources = providedResources;
    }


    @Override public <O extends @Nullable Serializable> void setCallback(Task<O> task, Consumer<O> function) {
        try(final StoreWriteTxn txn = store.writeTxn()) {
            txn.removeCallback(task.key());
        }
        callbacks.set(task, function);
    }

    @Override public void setCallback(TaskKey key, Consumer<@Nullable Serializable> function) {
        try(final StoreWriteTxn txn = store.writeTxn()) {
            txn.removeCallback(key);
        }
        callbacks.set(key, function);
    }

    @Override
    public <O extends Serializable> void setSerializableCallback(Task<O> task, SerializableConsumer<O> function) {
        final TaskKey key = task.key();
        callbacks.remove(key);
        try(final StoreWriteTxn txn = store.writeTxn()) {
            @SuppressWarnings("unchecked") final SerializableConsumer<Serializable> base = (SerializableConsumer<Serializable>)function;
            txn.setCallback(key, base);
        }
    }

    @Override public void setSerializableCallback(TaskKey key, SerializableConsumer<Serializable> function) {
        callbacks.remove(key);
        try(final StoreWriteTxn txn = store.writeTxn()) {
            txn.setCallback(key, function);
        }
    }

    @Override public void removeCallback(Task<?> task) {
        callbacks.remove(task);
        try(final StoreWriteTxn txn = store.writeTxn()) {
            txn.removeCallback(task.key());
        }
    }

    @Override public void removeCallback(TaskKey key) {
        callbacks.remove(key);
        try(final StoreWriteTxn txn = store.writeTxn()) {
            txn.removeCallback(key);
        }
    }

    @Override public void dropCallbacks() {
        callbacks.clear();
        try(final StoreWriteTxn txn = store.writeTxn()) {
            txn.dropCallbacks();
        }
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

    @Override public void unobserve(TaskKey key) {
        try(final StoreWriteTxn txn = store.writeTxn()) {
            Observability.explicitUnobserve(txn, key, tracer);
        }
    }

    @Override public void unobserve(Task<?> task) {
        unobserve(task.key());
    }

    @Override
    public void deleteUnobservedTasks(Predicate<Task<?>> shouldDeleteTask, BiPredicate<Task<?>, HierarchicalResource> shouldDeleteProvidedResource) throws IOException {
        try(final StoreWriteTxn txn = store.writeTxn()) {
            // Start with tasks that have no callers: these are either ExplicitlyObserved, or Unobserved.
            final Deque<TaskKey> tasksToDelete = new ArrayDeque<>(txn.getTasksWithoutCallers());
            while(!tasksToDelete.isEmpty()) {
                final TaskKey key = tasksToDelete.pop();
                if(!txn.getTaskObservability(key).isUnobserved()) {
                    // Do not delete observed tasks. This filters out ExplicitlyObserved tasks.
                    continue;
                }
                final Task<?> task = key.toTask(taskDefs, txn);
                if(!shouldDeleteTask.test(task)) {
                    // Do not delete tasks that the caller of this function does not want to delete.
                    continue;
                }
                final @Nullable TaskData deletedData = txn.deleteData(key);
                if(deletedData != null) {
                    // Delete provided resources.
                    for(ResourceProvideDep dep : deletedData.deps.resourceProvideDeps) {
                        final Resource resource = resourceService.getResource(dep.key);
                        if(resource instanceof HierarchicalResource) {
                            final HierarchicalResource hierarchicalResource = ((HierarchicalResource)resource);
                            if(shouldDeleteProvidedResource.test(task, hierarchicalResource)) {
                                hierarchicalResource.delete(true);
                            }
                        }
                    }

                    // Iterate the task requirements of the deleted task to continue deleting tasks.
                    deletedData.deps.taskRequireDeps
                        .stream()
                        // Filter out tasks that still have incoming callers.
                        .filter((d) -> txn.getCallersOf(d.callee).isEmpty())
                        // Push tasks onto the stack that have no incoming callers for deletion.
                        // The start of the while loop will ensure that only unobserved tasks will be deleted.
                        .forEach((d) -> tasksToDelete.push(d.callee));
                }
            }
        }
    }


    @Override public Set<ResourceKey> getProvidedResources() {
        return Collections.unmodifiableSet(this.providedResources);
    }


    @Override public void dropStore() {
        try(final StoreWriteTxn txn = store.writeTxn()) {
            txn.drop();
        }
    }


    protected <T extends Serializable> T handleException(Supplier<T> supplier) throws ExecException, InterruptedException {
        try {
            return supplier.get();
        } catch(CanceledException e) {
            throw e.toInterruptedException();
        } catch(UncheckedInterruptedException e) {
            throw e.interruptedException;
        } catch(UncheckedExecException e) {
            throw e.toChecked();
        }
    }

    protected void handleException(Runnable runnable) throws ExecException, InterruptedException {
        try {
            runnable.run();
        } catch(CanceledException e) {
            throw e.toInterruptedException();
        } catch(UncheckedInterruptedException e) {
            throw e.interruptedException;
        } catch(UncheckedExecException e) {
            throw e.toChecked();
        }
    }
}
