package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

/**
 * Serializable task, consisting of the [identifier of a task definition][id], and its [input].
 */
public class STask implements Serializable {
    public final String id;
    public final Serializable input;

    public STask(String id, Serializable input) {
        this.id = id;
        this.input = input;
    }

    public Task<?> toTask(TaskDefs taskDefs) {
        final @Nullable TaskDef<?, ?> taskDef = taskDefs.getTaskDef(id);
        if(taskDef == null) {
            throw new RuntimeException(
                "Cannot get task definition for id " + id + "; task definition with that id does not exist");
        }
        return new Task<>(taskDef, input);
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final STask sTask = (STask) o;
        if(!id.equals(sTask.id)) return false;
        return input.equals(sTask.input);

    }

    @Override public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + input.hashCode();
        return result;
    }

    public String toShortString(int maxLength) {
        return id + "(" + StringUtil.toShortString(input.toString(), maxLength) + ")";
    }

    @Override public String toString() {
        return toShortString(100);
    }
}
