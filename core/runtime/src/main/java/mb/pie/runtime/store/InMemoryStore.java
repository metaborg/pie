package mb.pie.runtime.store;

import mb.pie.api.TaskData;
import mb.pie.api.TaskKey;
import mb.pie.api.TaskRequireDep;
import mb.pie.runtime.graph.DAG;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.HashSet;

/**
 * More optimized version of in-memory store. Overrides several methods from {@link InMemoryStoreBase} with more
 * performant alternatives.
 */
public class InMemoryStore extends InMemoryStoreBase {
    private DAG<TaskKey> taskRequireGraph = new DAG<>();

    @Override public boolean hasDependencyOrderBefore(TaskKey caller, TaskKey callee) {
        return taskRequireGraph.getTopologicalIndex(caller) < taskRequireGraph.getTopologicalIndex(callee);
    }

    @Override public void setTaskRequires(TaskKey caller, Collection<TaskRequireDep> newDeps) {
        final HashSet<TaskKey> added = new HashSet<>();
        final HashSet<TaskKey> removed = new HashSet<>();

        // Remove old task requirements and fill `added` and `removed`.
        final @Nullable Collection<TaskRequireDep> oldTaskDeps = this.taskRequires.remove(caller);
        if(oldTaskDeps != null) {
            for(TaskRequireDep newDep : newDeps) { // Add new deps to `added`.
                added.add(newDep.callee);
            }
            for(TaskRequireDep oldDep : oldTaskDeps) {
                final TaskKey oldCallee = oldDep.callee;
                removed.add(oldCallee); // Add old deps to `removed`.
                added.remove(oldCallee); // Remove old dep from `added`.
                getOrPutEmptyHashSet(callersOf, oldCallee).remove(caller);
            }
            // `added` now contains all keys for tasks which `caller` newly depends on.
            for(TaskRequireDep newDep : newDeps) { // Remove new deps from `removed`.
                removed.remove(newDep.callee);
            }
            // `removed` now contains all keys for tasks which `caller` does not depend on any more.
        } else {
            for(TaskRequireDep dep : newDeps) {
                added.add(dep.callee);
            }
        }

        // Add new task requirements.
        this.taskRequires.put(caller, newDeps);
        for(TaskRequireDep taskRequire : newDeps) {
            getOrPutEmptyHashSet(callersOf, taskRequire.callee).add(caller);
        }

        // Update dependency graph.
        taskRequireGraph.addVertex(caller);
        for(TaskKey callee : added) {
            taskRequireGraph.addVertex(callee);
            taskRequireGraph.addEdge(caller, callee);
        }
        for(TaskKey callee : removed) {
            taskRequireGraph.removeEdge(caller, callee);
        }
    }


    @Override public @Nullable TaskData deleteData(TaskKey key) {
        taskRequireGraph.removeVertex(key);
        return super.deleteData(key);
    }

    @Override public void drop() {
        super.drop();
        taskRequireGraph = new DAG<>();
    }


    @Override public String toString() {
        return "InMemoryStore()";
    }
}
