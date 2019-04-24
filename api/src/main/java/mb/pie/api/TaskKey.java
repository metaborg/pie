package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

/**
 * Key of a task, consisting of a [task definition identifier][id] and a [key].
 */
public class TaskKey implements Serializable {
    public final String id;
    public final Serializable key;

    public TaskKey(String id, Serializable key) {
        this.id = id;
        this.key = key;
    }

    public Task<?> toTask(TaskDefs taskDefs, StoreReadTxn txn) {
        final @Nullable TaskDef<?, ?> taskDef = taskDefs.getTaskDef(id);
        if(taskDef == null) {
            throw new RuntimeException(
                "Cannot get task definition for task key " + this + "; task definition with id " + id + " does not exist");
        }
        final @Nullable Serializable input = txn.input(this);
        if(input == null) {
            throw new RuntimeException("Cannot get task for task key " + this + " ; input object does not exist");
        }
        return new Task<>(taskDef, input);
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final TaskKey taskKey = (TaskKey) o;
        if(!id.equals(taskKey.id)) return false;
        return key.equals(taskKey.key);
    }

    @Override public int hashCode() {
        // PERF TODO: cache hashCode, as in the Kotlin implementation?
        int result = id.hashCode();
        result = 31 * result + key.hashCode();
        return result;
    }

    public String toShortString(int maxLength) {
        return "#" + id + "(" + StringUtil.toShortString(key.toString(), maxLength) + ")";
    }

    @Override public String toString() {
        return toShortString(100);
    }
}
