package mb.pie.runtime.taskdefs;

import mb.pie.api.TaskDef;
import mb.pie.api.TaskDefs;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Task definitions from parent and child task definitions.
 */
public class CompositeTaskDefs implements TaskDefs {
    private final List<TaskDefs> parentTaskDefs;
    private final TaskDefs childTaskDefs;

    public CompositeTaskDefs(Collection<TaskDefs> parentTaskDefs, TaskDefs childTaskDefs) {
        this.parentTaskDefs = new ArrayList<>(parentTaskDefs);
        this.childTaskDefs = childTaskDefs;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override public @Nullable TaskDef<?, ?> getTaskDef(String id) {
        // Check parent first, such that child task definitions cannot override those from the parent.
        return parentTaskDefs
            .stream()
            .map(taskDefs -> taskDefs.getTaskDef(id))
            .filter(Objects::nonNull)
            .findFirst()
            .orElseGet(() -> (TaskDef) childTaskDefs.getTaskDef(id)); // Cast to remove type error due to incompatible capture type variables
    }

    @Override public boolean exists(String id) {
        return parentTaskDefs.stream().anyMatch(taskDefs -> taskDefs.exists(id)) || childTaskDefs.exists(id);
    }
}
