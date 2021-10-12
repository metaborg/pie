package mb.pie.runtime.store;

import mb.pie.api.Task;
import mb.pie.api.TaskData;
import mb.pie.api.TaskKey;
import mb.pie.api.TaskRequireDep;
import mb.pie.runtime.graph.DAG;
import mb.pie.runtime.graph.DefaultEdge;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;

/**
 * More optimized version of in-memory store. Overrides several methods from {@link InMemoryStoreBase} with more
 * performant alternatives.
 */
public class InMemoryStore extends InMemoryStoreBase {
    private DAG<TaskKey> taskRequireGraph = new DAG<>();

    @Override public boolean hasDependencyOrderBefore(TaskKey caller, TaskKey callee) {
        return this.taskRequireGraph.getTopologicalIndex(caller) < this.taskRequireGraph.getTopologicalIndex(callee);
    }


    @Override public @Nullable TaskData resetTask(Task<?> task) {
        final TaskKey key = task.key();
        // A task that does not exist may be reset, so a vertex must be created to ensure there is one in the graph.
        //
        // `addVertex` returns `true` if the vertex is new. In that case, we can skip removing outgoing edges as a new
        // vertex cannot have any edges.
        if(!this.taskRequireGraph.addVertex(key)) {
            removeOutgoingEdgesOf(key);
        }
        return super.resetTask(task);
    }

    private void removeOutgoingEdgesOf(TaskKey key) {
        // Remove all outgoing edges of vertex `key`, as they correspond to task require dependencies.
        //
        // Copy collection, as the collection returned by `outgoingEdgesOf` is live and will cause
        // `ConcurrentModificationException`s when passed to `removeAllEdges` directly, because it iterates the
        // collection while removing elements.
        final ArrayList<DefaultEdge> outgoingEdges = new ArrayList<>(this.taskRequireGraph.outgoingEdgesOf(key));
        this.taskRequireGraph.removeAllEdges(outgoingEdges);
    }

    @Override public void addTaskRequire(TaskKey caller, TaskKey callee) {
        doAddTaskRequire(caller, callee);
        super.addTaskRequire(caller, callee);
    }

    private void doAddTaskRequire(TaskKey caller, TaskKey callee) {
        this.taskRequireGraph.addVertex(callee);
        this.taskRequireGraph.addEdge(caller, callee);
    }


    @Override public void restoreData(TaskKey key, TaskData data) {
        removeOutgoingEdgesOf(key);
        for(TaskRequireDep dep : data.deps.taskRequireDeps) {
            doAddTaskRequire(key, dep.callee);
        }
        super.restoreData(key, data);
    }

    @Override public @Nullable TaskData deleteData(TaskKey key) {
        this.taskRequireGraph.removeVertex(key);
        return super.deleteData(key);
    }


    @Override public void drop() {
        super.drop();
        this.taskRequireGraph = new DAG<>();
    }


    @Override public String toString() {
        return "InMemoryStore()";
    }
}
