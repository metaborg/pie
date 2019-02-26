package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

/**
 * Executable task, consisting of a [task definition][TaskDef] and its [input].
 */
public class Task<I extends Serializable, O extends @Nullable Serializable> {
    public final TaskDef<I, O> taskDef;
    public final I input;


    public Task(TaskDef<I, O> taskDef, I input) {
        this.taskDef = taskDef;
        this.input = input;
    }


    public String getId() {
        return taskDef.getId();
    }

    public TaskKey key() {
        final Serializable key = taskDef.key(input);
        return new TaskKey(taskDef.getId(), key);
    }

    public @Nullable O exec(ExecContext ctx) throws ExecException, InterruptedException {
        return taskDef.exec(ctx, input);
    }

    public String desc(int maxLength) {
        return taskDef.desc(input, maxLength);
    }


    public STask<I> toSTask() {
        return new STask<>(taskDef.getId(), input);
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final Task<?, ?> task = (Task<?, ?>) o;
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

