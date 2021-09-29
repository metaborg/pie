package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Task definitions from a map.
 */
public class MapTaskDefs implements TaskDefs {
    private final HashMap<String, TaskDef<?, ?>> taskDefs;

    public MapTaskDefs() {
        this.taskDefs = new HashMap<>();
    }

    /**
     * @throws IllegalArgumentException when there is a duplicate {@link TaskDef#getId() task definition identifier}.
     */
    public MapTaskDefs(TaskDefs taskDefs) {
        this.taskDefs = new HashMap<>();
        for(TaskDef<?, ?> taskDef : taskDefs.getAllTaskDefs()) {
            add(taskDef);
        }
    }

    /**
     * @throws IllegalArgumentException when there is a duplicate {@link TaskDef#getId() task definition identifier}.
     */
    public MapTaskDefs(TaskDef<?, ?>... taskDefs) {
        this.taskDefs = new HashMap<>();
        for(TaskDef<?, ?> taskDef : taskDefs) {
            add(taskDef);
        }
    }

    /**
     * @throws IllegalArgumentException when there is a duplicate {@link TaskDef#getId() task definition identifier}.
     */
    public MapTaskDefs(Iterable<TaskDef<?, ?>> taskDefs) {
        this.taskDefs = new HashMap<>();
        for(TaskDef<?, ?> taskDef : taskDefs) {
            add(taskDef);
        }
    }

    public MapTaskDefs(Map<String, TaskDef<?, ?>> taskDefs) {
        this.taskDefs = new HashMap<>(taskDefs);
    }

    public MapTaskDefs(HashMap<String, TaskDef<?, ?>> taskDefs) {
        this.taskDefs = taskDefs;
    }


    @Override public @Nullable TaskDef<?, ?> getTaskDef(String id) {
        return taskDefs.get(id);
    }

    @Override public Iterable<TaskDef<?, ?>> getAllTaskDefs() {
        return taskDefs.values();
    }

    @Override public boolean exists(String id) {
        return taskDefs.containsKey(id);
    }


    /**
     * Adds a task definition.
     *
     * @param taskDef Task definition to add.
     * @throws IllegalArgumentException when there is a duplicate {@link TaskDef#getId() task definition identifier}.
     */
    public void add(TaskDef<?, ?> taskDef) {
        final String id = taskDef.getId();
        final @Nullable TaskDef<?, ?> existing = taskDefs.put(id, taskDef);
        if(existing != null) {
            throw new IllegalArgumentException("Task definition with ID '" + id + "' (class " + taskDef.getClass().getName() + ") already exists: '" + existing + "' (class " + existing.getClass().getName() + ")");
        }
    }

    /**
     * Adds all task definitions.
     *
     * @param taskDefs Task definitions to add.
     * @throws IllegalArgumentException when there is a duplicate {@link TaskDef#getId() task definition identifier}.
     */
    public void addAll(Iterable<TaskDef<?, ?>> taskDefs) {
        for(TaskDef<?, ?> taskDef : taskDefs) {
            add(taskDef);
        }
    }

    public void remove(TaskDef<?, ?> taskDef) {
        taskDefs.remove(taskDef.getId());
    }

    public void remove(String id) {
        taskDefs.remove(id);
    }

    public void clear() {
        taskDefs.clear();
    }
}
