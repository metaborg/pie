package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Objects;

/**
 * Serializable task definition wrapper for use in task inputs and outputs.
 * <p>
 * Use this wrapper when you need to pass task definitions as data in task inputs or outputs, because task inputs and
 * outputs need to be {@link Serializable}. A regular {@link TaskDef task definition} is not serializable, as that would
 * limit instantiation of the class to only serializable data. Therefore, this wrapper makes it serializable by just
 * storing the {@link #id identifier of the task definition}.
 * <p>
 * Prefer {@link TaskDef} where possible, as turning this wrapper into a real {@link TaskDef} requires a lookup and a
 * cast, which is less efficient.
 *
 * @param <I> Type of input objects of the task definition. Must be {@link Serializable} and may NOT be {@code null}.
 * @param <O> Type of output objects of the task definition. Must be {@link Serializable} but may be {@code null}.
 * @see TaskDef
 * @see STask
 */
public class STaskDef<I extends Serializable, O extends @Nullable Serializable> implements Serializable {
    public final String id;

    public STaskDef(TaskDef<I, O> taskDef) {
        this.id = taskDef.getId();
    }

    public STaskDef(String id) {
        this.id = id;
    }

    /**
     * Gets the {@link TaskDef task definition} corresponding to this serializable task definition wrapper. The
     * resulting task definition is looked up and then casted to the correct input and output types. This cast is
     * unchecked, which may result in exceptions when the returned  {@link TaskDef task definition} is used.
     *
     * @param taskDefs {@link TaskDefs Task definition collection} to lookup the real {@link TaskDef task definition}
     *                 from.
     * @return {@link TaskDef Task definition} corresponding to this wrapper.
     */
    public TaskDef<I, O> toTaskDef(TaskDefs taskDefs) {
        @SuppressWarnings("unchecked") final @Nullable TaskDef<I, O> taskDef = (TaskDef<I, O>)taskDefs.getTaskDef(id);
        if(taskDef == null) {
            throw new RuntimeException(
                "Cannot get task definition for id " + id + "; task definition with that id does not exist");
        }
        return taskDef;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final STaskDef sTaskDef = (STaskDef)o;
        return id.equals(sTaskDef.id);
    }

    @Override public int hashCode() {
        return Objects.hash(id);
    }

    @Override public String toString() {
        return id;
    }
}
