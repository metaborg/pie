package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public final class TaskData {
    public final Serializable input;
    public final @Nullable Serializable internalObject;
    public final @Nullable Serializable output;
    public final Observability taskObservability;
    public final TaskDeps deps;


    public TaskData(
        Serializable input,
        @Nullable Serializable internalObject,
        @Nullable Serializable output,
        Observability taskObservability,
        TaskDeps deps
    ) {
        this.input = input;
        this.internalObject = internalObject;
        this.output = output;
        this.taskObservability = taskObservability;
        this.deps = deps;
    }


    public TaskData withTaskObservability(Observability taskObservability) {
        return new TaskData(input, internalObject, output, taskObservability, deps);
    }


    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final TaskData taskData = (TaskData)o;
        if(!input.equals(taskData.input)) return false;
        if(internalObject != null ? !internalObject.equals(taskData.internalObject) : taskData.internalObject != null)
            return false;
        if(output != null ? !output.equals(taskData.output) : taskData.output != null) return false;
        if(taskObservability != taskData.taskObservability) return false;
        return deps.equals(taskData.deps);
    }

    @Override public int hashCode() {
        int result = input.hashCode();
        result = 31 * result + (internalObject != null ? internalObject.hashCode() : 0);
        result = 31 * result + (output != null ? output.hashCode() : 0);
        result = 31 * result + taskObservability.hashCode();
        result = 31 * result + deps.hashCode();
        return result;
    }

    @Override public String toString() {
        return "TaskData{" +
            "input=" + input +
            ", internalObject=" + internalObject +
            ", output=" + output +
            ", taskObservability=" + taskObservability +
            ", deps=" + deps +
            '}';
    }
}
