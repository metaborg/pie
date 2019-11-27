package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;

/**
 * Task definitions from a map.
 */
public class MapTaskDefs implements TaskDefs {
    private final HashMap<String, TaskDef<?, ?>> taskDefs;

    public MapTaskDefs() {
        this.taskDefs = new HashMap<>();
    }

    public MapTaskDefs(Iterable<TaskDef<?, ?>> taskDefs) {
        this.taskDefs = new HashMap<>();
        for(TaskDef<?, ?> taskDef : taskDefs) {
            this.taskDefs.put(taskDef.getId(), taskDef);
        }
    }

    public MapTaskDefs(HashMap<String, TaskDef<?, ?>> taskDefs) {
        this.taskDefs = taskDefs;
    }

    @Override public @Nullable TaskDef<?, ?> getTaskDef(String id) {
        return taskDefs.get(id);
    }

    @Override public boolean exists(String id) {
        return taskDefs.containsKey(id);
    }

    public void add(TaskDef<?, ?> taskDef) {
        taskDefs.put(taskDef.getId(), taskDef);
    }

    public void remove(TaskDef<?, ?> taskDef) {
        taskDefs.remove(taskDef.getId());
    }

    public void remove(String id) {
        taskDefs.remove(id);
    }
}
