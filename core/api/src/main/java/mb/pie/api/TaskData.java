package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;

public final class TaskData {
    public final Serializable input;
    public final @Nullable Serializable output;
    public final Observability taskObservability;
    public final Collection<TaskRequireDep> taskRequires;
    public final Collection<ResourceRequireDep> resourceRequires;
    public final Collection<ResourceProvideDep> resourceProvides;


    public TaskData(
        Serializable input,
        @Nullable Serializable output,
        Observability taskObservability,
        Collection<TaskRequireDep> taskRequires,
        Collection<ResourceRequireDep> resourceRequires,
        Collection<ResourceProvideDep> resourceProvides
    ) {
        this.input = input;
        this.output = output;
        this.taskObservability = taskObservability;
        this.taskRequires = taskRequires;
        this.resourceRequires = resourceRequires;
        this.resourceProvides = resourceProvides;
    }


    public TaskData withTaskObservability(Observability taskObservability) {
        return new TaskData(input, output, taskObservability, taskRequires, resourceRequires, resourceProvides);
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final TaskData taskData = (TaskData)o;
        if(!input.equals(taskData.input)) return false;
        if(!Objects.equals(output, taskData.output)) return false;
        if(!taskObservability.equals(taskData.taskObservability)) return false;
        if(!taskRequires.equals(taskData.taskRequires)) return false;
        if(!resourceRequires.equals(taskData.resourceRequires)) return false;
        return resourceProvides.equals(taskData.resourceProvides);
    }

    @Override public int hashCode() {
        int result = input.hashCode();
        result = 31 * result + (output != null ? output.hashCode() : 0);
        result = 31 * result + taskObservability.hashCode();
        result = 31 * result + taskRequires.hashCode();
        result = 31 * result + resourceRequires.hashCode();
        result = 31 * result + resourceProvides.hashCode();
        return result;
    }

    @Override public String toString() {
        return "TaskData(" +
            "  input             = " + input +
            ", output            = " + output +
            ", taskObservability = " + taskObservability +
            ", taskRequires      = " + taskRequires +
            ", resourceRequires  = " + resourceRequires +
            ", resourceProvides  = " + resourceProvides +
            ')';
    }
}
