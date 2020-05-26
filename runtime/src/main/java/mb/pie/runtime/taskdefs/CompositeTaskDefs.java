package mb.pie.runtime.taskdefs;

import mb.pie.api.TaskDef;
import mb.pie.api.TaskDefs;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Task definitions from parent and child task definitions.
 */
public class CompositeTaskDefs implements TaskDefs {
    private final TaskDefs parentTaskDefs;
    private final TaskDefs childTaskDefs;

    public CompositeTaskDefs(TaskDefs parentTaskDefs, TaskDefs childTaskDefs) {
        this.parentTaskDefs = parentTaskDefs;
        this.childTaskDefs = childTaskDefs;
    }

    @Override public @Nullable TaskDef<?, ?> getTaskDef(String id) {
        // Check parent first, such that child task definitions cannot override those from the parent.
        final @Nullable TaskDef<?, ?> taskDef = parentTaskDefs.getTaskDef(id);
        if(taskDef != null) {
            return taskDef;
        } else {
            return childTaskDefs.getTaskDef(id);
        }
    }

    @Override public boolean exists(String id) {
        return parentTaskDefs.exists(id) || childTaskDefs.exists(id);
    }
}
