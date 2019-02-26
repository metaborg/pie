package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.ArrayList;

public final class TaskData<I extends Serializable, O extends @Nullable Serializable> {
    public final I input;
    public final @Nullable O output;
    public final ArrayList<TaskRequireDep> taskRequires;
    public final ArrayList<ResourceRequireDep> resourceRequires;
    public final ArrayList<ResourceProvideDep> resourceProvides;

    public TaskData(I input, @Nullable O output, ArrayList<TaskRequireDep> taskRequires, ArrayList<ResourceRequireDep> resourceRequires, ArrayList<ResourceProvideDep> resourceProvides) {
        this.input = input;
        this.output = output;
        this.taskRequires = taskRequires;
        this.resourceRequires = resourceRequires;
        this.resourceProvides = resourceProvides;
    }


    public <IC extends Serializable, OC extends @Nullable Serializable> TaskData<IC, OC> cast() {
        return (TaskData<IC, OC>) this;
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final TaskData<?, ?> taskData = (TaskData<?, ?>) o;
        if(!input.equals(taskData.input)) return false;
        if(output != null ? !output.equals(taskData.output) : taskData.output != null) return false;
        if(!taskRequires.equals(taskData.taskRequires)) return false;
        if(!resourceRequires.equals(taskData.resourceRequires)) return false;
        return resourceProvides.equals(taskData.resourceProvides);
    }

    @Override public int hashCode() {
        int result = input.hashCode();
        result = 31 * result + (output != null ? output.hashCode() : 0);
        result = 31 * result + taskRequires.hashCode();
        result = 31 * result + resourceRequires.hashCode();
        result = 31 * result + resourceProvides.hashCode();
        return result;
    }

    @Override public String toString() {
        return "TaskData(" +
            "input=" + input +
            ", output=" + output +
            ", taskRequires=" + taskRequires +
            ", resourceRequires=" + resourceRequires +
            ", resourceProvides=" + resourceProvides +
            ')';
    }
}
