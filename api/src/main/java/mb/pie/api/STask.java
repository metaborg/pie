package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

/**
 * Serializable task instance wrapper for use in task inputs and outputs.
 * <p>
 * Use this wrapper when you need to pass task instances as data in task inputs or outputs, because task inputs and
 * outputs need to be {@link Serializable}. A regular {@link Task task instance} is not serializable, as it carries
 * around a non-serializable {@link TaskDef task definition}. Therefore, this wrapper makes a task serializable by just
 * storing the {@link #id identifier of the task definition}.
 * <p>
 * Prefer {@link Task} where possible, as turning this wrapper into a real {@link Task} requires a lookup and a cast,
 * which is less efficient.
 *
 * @param <O> Type of output object that this task produces when it is executed. Must be {@link Serializable} but may be
 *            {@code null}.
 * @see Task
 * @see STaskDef
 */
public class STask<O extends @Nullable Serializable> implements Supplier<O>, Serializable {
    public final String id;
    public final Serializable input;

    public <I extends Serializable> STask(TaskDef<I, O> taskDef, I input) {
        this.id = taskDef.getId();
        this.input = input;
    }

    public <I extends Serializable> STask(STaskDef<I, O> sTaskDef, I input) {
        this.id = sTaskDef.id;
        this.input = input;
    }

    public STask(String id, Serializable input) {
        this.id = id;
        this.input = input;
    }

    /**
     * Gets the {@link Task task instance} corresponding to this serializable task instance wrapper. The resulting
     * {@link Task task instance} is created by looking up the {@link TaskDef task definition} and casting it to the
     * correct input and output types. This cast is unchecked, which may result in exceptions when the returned {@link
     * Task task instance} is used.
     *
     * @param taskDefs {@link TaskDefs Task definition collection} to lookup the real {@link TaskDef task definition}
     *                 from which is used to build the {@link Task task instance}.
     * @return {@link Task Task instance} corresponding to this wrapper.
     */
    public Task<O> toTask(TaskDefs taskDefs) {
        @SuppressWarnings("unchecked") final @Nullable TaskDef<?, O> taskDef = (TaskDef<?, O>)taskDefs.getTaskDef(id);
        if(taskDef == null) {
            throw new RuntimeException(
                "Cannot get task definition for id " + id + "; task definition with that id does not exist");
        }
        return new Task<>(taskDef, input);
    }

    @Override public O get(ExecContext context) throws ExecException, InterruptedException {
        return context.require(this);
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final STask sTask = (STask)o;
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
