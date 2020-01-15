package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

/**
 * A task instance, which wraps a {@link TaskDef task definition} and its corresponding {@code input}.
 * <p>
 * Although all fields and methods are public, users of this library may only call documented constructors and/or
 * methods for sound incrementality.
 *
 * @param <O> Type of output object that this task produces when it is executed. Must be {@link Serializable} but may be
 *            {@code null}.
 */
public class Task<O extends @Nullable Serializable> {
    public final TaskDef<Serializable, O> taskDef;
    public final Serializable input;

    /**
     * Creates a task from {@code taskDef} and {@code input}.
     *
     * @param taskDef Task definition to create a task for.
     * @param input   Input object to create a task for. The (super)type of this object MUST BE {@code I}. This cannot
     *                be statically checked due to limitations of Java's generics (or my lack of understanding of
     *                them).
     * @param <I>     Type of input objects.
     */
    public <I extends Serializable> Task(TaskDef<I, O> taskDef, Serializable input) {
        @SuppressWarnings("unchecked") final TaskDef<Serializable, O> inputErasedTaskDef = (TaskDef<Serializable, O>)taskDef;
        this.taskDef = inputErasedTaskDef;
        this.input = input;
    }

    /**
     * Creates a {@link STask serializable task} for this task.
     *
     * @return {@link STask Serializable task} for this task.
     */
    public STask<O> toSerializableTask() {
        return new STask<>(taskDef, input);
    }


    public String getId() {
        return taskDef.getId();
    }

    public TaskKey key() {
        final Serializable key = taskDef.key(input);
        return new TaskKey(taskDef.getId(), key);
    }

    public O exec(ExecContext ctx) throws Exception {
        return taskDef.exec(ctx, input);
    }

    public String desc(int maxLength) {
        return taskDef.desc(input, maxLength);
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final Task<?> task = (Task<?>)o;
        if(!taskDef.getId().equals(task.taskDef.getId())) return false; // Note: comparing TaskDef IDs.
        return input.equals(task.input);
    }

    @Override public int hashCode() {
        int result = taskDef.getId().hashCode(); // Note: hashing TaskDef IDs.
        result = 31 * result + input.hashCode();
        return result;
    }

    @Override public String toString() {
        return desc(100);
    }
}

