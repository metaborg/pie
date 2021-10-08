package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Collection;
import java.util.Objects;

public final class TaskData {
    public final Serializable input;
    public final @Nullable Serializable output;
    public final Observability taskObservability;
    public final Collection<TaskRequireDep> taskRequireDeps;
    public final Collection<ResourceRequireDep> resourceRequireDeps;
    public final Collection<ResourceProvideDep> resourceProvideDeps;


    public TaskData(
        Serializable input,
        @Nullable Serializable output,
        Observability taskObservability,
        Collection<TaskRequireDep> taskRequireDeps,
        Collection<ResourceRequireDep> resourceRequireDeps,
        Collection<ResourceProvideDep> resourceProvideDeps
    ) {
        this.input = input;
        this.output = output;
        this.taskObservability = taskObservability;
        this.taskRequireDeps = taskRequireDeps;
        this.resourceRequireDeps = resourceRequireDeps;
        this.resourceProvideDeps = resourceProvideDeps;
    }


    public TaskData withTaskObservability(Observability taskObservability) {
        return new TaskData(input, output, taskObservability, taskRequireDeps, resourceRequireDeps, resourceProvideDeps);
    }


    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final TaskData taskData = (TaskData)o;
        if(!input.equals(taskData.input)) return false;
        if(!Objects.equals(output, taskData.output)) return false;
        if(!taskObservability.equals(taskData.taskObservability)) return false;
        if(!taskRequireDeps.equals(taskData.taskRequireDeps)) return false;
        if(!resourceRequireDeps.equals(taskData.resourceRequireDeps)) return false;
        return resourceProvideDeps.equals(taskData.resourceProvideDeps);
    }

    @Override public int hashCode() {
        int result = input.hashCode();
        result = 31 * result + (output != null ? output.hashCode() : 0);
        result = 31 * result + taskObservability.hashCode();
        result = 31 * result + taskRequireDeps.hashCode();
        result = 31 * result + resourceRequireDeps.hashCode();
        result = 31 * result + resourceProvideDeps.hashCode();
        return result;
    }

    @Override public String toString() {
        return "TaskData(" +
            "  input             = " + input +
            ", output            = " + output +
            ", taskObservability = " + taskObservability +
            ", taskRequires      = " + taskRequireDeps +
            ", resourceRequires  = " + resourceRequireDeps +
            ", resourceProvides  = " + resourceProvideDeps +
            ')';
    }
}
