package mb.pie.runtime;

import mb.pie.api.Observability;
import mb.pie.api.ResourceProvideDep;
import mb.pie.api.Session;
import mb.pie.api.Store;
import mb.pie.api.StoreWriteTxn;
import mb.pie.api.Task;
import mb.pie.api.TaskData;
import mb.pie.api.TaskDefs;
import mb.pie.api.TaskKey;
import mb.resource.Resource;
import mb.resource.ResourceService;
import mb.resource.hierarchical.HierarchicalResource;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public abstract class SessionImpl implements Session {
    protected final TaskDefs taskDefs;
    protected final ResourceService resourceService;
    protected final Store store;


    public SessionImpl(TaskDefs taskDefs, ResourceService resourceService, Store store) {
        this.taskDefs = taskDefs;
        this.resourceService = resourceService;
        this.store = store;
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
    public void deleteUnobservedTasks(Predicate<Task<?>> shouldDeleteTask, BiPredicate<Task<?>, Resource> shouldDeleteProvidedResource) throws IOException {
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
                if(!shouldDeleteTask.test(task)) {
                    // Do not delete tasks that the caller of this function does not want to delete.
                    continue;
                }
                final @Nullable TaskData deletedData = txn.deleteData(key);
                if(deletedData != null) {
                    // Delete provided resources.
                    for(ResourceProvideDep dep : deletedData.resourceProvides) {
                        final Resource resource = resourceService.getResource(dep.key);
                        if(shouldDeleteProvidedResource.test(task, resource)) {
                            if(resource instanceof HierarchicalResource) {
                                ((HierarchicalResource)resource).delete();
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
