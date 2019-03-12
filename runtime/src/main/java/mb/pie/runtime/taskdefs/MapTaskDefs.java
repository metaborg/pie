package mb.pie.runtime.taskdefs;

import mb.pie.api.TaskDef;
import mb.pie.api.TaskDefs;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Task definitions from a map.
 */
public class MapTaskDefs implements TaskDefs {
    private final HashMap<String, TaskDef<?, ?>> taskDefs;


    public MapTaskDefs() {
        this.taskDefs = new HashMap<>();
    }

    public MapTaskDefs(HashMap<String, TaskDef<?, ?>> taskDefs) {
        this.taskDefs = taskDefs;
    }


    @Override public <I extends Serializable, O extends Serializable> @Nullable TaskDef<I, O> getTaskDef(String id) {
        @SuppressWarnings("unchecked") final @Nullable TaskDef<I, O> taskDef = (@Nullable TaskDef<I, O>) taskDefs.get(id);
        return taskDef;
    }


    public void add(String id, TaskDef<?, ?> taskDef) {
        taskDefs.put(id, taskDef);
    }

    public void remove(String id) {
        taskDefs.remove(id);
    }
}
