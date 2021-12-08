package mb.pie.runtime.taskdefs;

import mb.pie.api.MapTaskDefs;
import mb.pie.api.TaskDef;
import mb.pie.api.TaskDefs;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;

/**
 * Task definitions from parent and child task definitions.
 */
public class CompositeTaskDefs implements TaskDefs {
    private final MapTaskDefs taskDefs;

    /**
     * @throws IllegalArgumentException when there is a duplicate {@link TaskDef#getId() task definition identifier}.
     */
    public CompositeTaskDefs(Collection<TaskDefs> parentTaskDefs, TaskDefs childTaskDefs) {
        this.taskDefs = new MapTaskDefs(childTaskDefs);
        for(TaskDefs taskDefs : parentTaskDefs) {
            this.taskDefs.addAll(taskDefs.getAllTaskDefs());
        }
    }

    @Override public @Nullable TaskDef<?, ?> getTaskDef(String id) {
        return taskDefs.getTaskDef(id);
    }

    @Override public Iterable<TaskDef<?, ?>> getAllTaskDefs() {
        return taskDefs.getAllTaskDefs();
    }

    @Override public boolean exists(String id) {
        return taskDefs.exists(id);
    }
}
