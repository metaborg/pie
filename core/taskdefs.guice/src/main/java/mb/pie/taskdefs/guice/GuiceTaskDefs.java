package mb.pie.taskdefs.guice;

import com.google.inject.Inject;
import mb.pie.api.MapTaskDefs;
import mb.pie.api.TaskDef;
import mb.pie.api.TaskDefs;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;

/**
 * Task definitions implementation that injects task definition binding created from {@link TaskDefsModule}.
 */
public class GuiceTaskDefs implements TaskDefs {
    private final MapTaskDefs taskDefs;

    /**
     * @throws IllegalArgumentException when there is a duplicate {@link TaskDef#getId() task definition identifier}.
     */
    @Inject public GuiceTaskDefs(Map<String, TaskDef<?, ?>> taskDefs) {
        this.taskDefs = new MapTaskDefs(taskDefs);
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
