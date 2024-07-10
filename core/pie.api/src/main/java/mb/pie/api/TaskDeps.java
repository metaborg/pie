package mb.pie.api;

import java.util.Collection;

public class TaskDeps {
    public final Collection<TaskRequireDep> taskRequireDeps;
    public final Collection<ResourceRequireDep> resourceRequireDeps;
    public final Collection<ResourceProvideDep> resourceProvideDeps;

    public TaskDeps(
        Collection<TaskRequireDep> taskRequireDeps,
        Collection<ResourceRequireDep> resourceRequireDeps,
        Collection<ResourceProvideDep> resourceProvideDeps
    ) {
        this.taskRequireDeps = taskRequireDeps;
        this.resourceRequireDeps = resourceRequireDeps;
        this.resourceProvideDeps = resourceProvideDeps;
    }
}
