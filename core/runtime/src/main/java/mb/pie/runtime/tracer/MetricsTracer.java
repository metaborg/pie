package mb.pie.runtime.tracer;

import mb.pie.api.InconsistentResourceProvide;
import mb.pie.api.InconsistentResourceRequire;
import mb.pie.api.InconsistentTaskRequire;
import mb.pie.api.ResourceProvideDep;
import mb.pie.api.ResourceRequireDep;
import mb.pie.api.Task;
import mb.pie.api.TaskKey;
import mb.pie.api.TaskRequireDep;
import mb.pie.api.exec.ExecReason;
import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamper;
import mb.resource.Resource;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.HashSet;

public class MetricsTracer extends EmptyTracer {
    public static class Report {
        public long totalProvidedResources = 0;
        public HashSet<ResourceKey> providedResources = new HashSet<>();
        public long totalRequiredResources = 0;
        public HashMap<ResourceKey, Long> requiredPerResource = new HashMap<>();
        public long totalRequiredTasks = 0;
        public HashMap<String, Long> requiredPerTaskDefinition = new HashMap<>();

        public boolean hasResourceBeenProvided(ResourceKey resourceKey) {
            return providedResources.contains(resourceKey);
        }

        public boolean hasResourceBeenRequired(ResourceKey resourceKey) {
            return getResourceRequiredCount(resourceKey) != 0;
        }

        public long getResourceRequiredCount(ResourceKey resourceKey) {
            return requiredPerResource.getOrDefault(resourceKey, 0L);
        }

        public boolean hasTaskDefBeenRequired(String taskDefId) {
            return getTaskDefRequiredCount(taskDefId) != 0;
        }

        public long getTaskDefRequiredCount(String taskDefId) {
            return requiredPerTaskDefinition.getOrDefault(taskDefId, 0L);
        }


        public long totalCheckedProvidedResourceDependencies = 0;
        public HashMap<ResourceKey, Long> checkedProvidedPerResource = new HashMap<>();
        public long totalCheckedRequiredResourceDependencies = 0;
        public HashMap<ResourceKey, Long> checkedRequiredPerResource = new HashMap<>();
        public long totalCheckedRequiredTaskDependencies = 0;
        public HashMap<String, Long> checkedRequiredPerTaskDefinition = new HashMap<>();

        public boolean hasResourceProvideDependnecyBeenChecked(ResourceKey resourceKey) {
            return getResourceProvideDependencyCheckCount(resourceKey) != 0;
        }

        public long getResourceProvideDependencyCheckCount(ResourceKey resourceKey) {
            return checkedProvidedPerResource.getOrDefault(resourceKey, 0L);
        }

        public boolean hasResourceRequireDependencyBeenChecked(ResourceKey resourceKey) {
            return getResourceRequireDependencyCheckCount(resourceKey) != 0;
        }

        public long getResourceRequireDependencyCheckCount(ResourceKey resourceKey) {
            return checkedRequiredPerResource.getOrDefault(resourceKey, 0L);
        }

        public boolean hasTaskDefRequireDependencyBeenChecked(String taskDefId) {
            return getTaskDefRequireDependencyCheckCount(taskDefId) != 0;
        }

        public long getTaskDefRequireDependencyCheckCount(String taskDefId) {
            return requiredPerTaskDefinition.getOrDefault(taskDefId, 0L);
        }


        public long totalExecutedTasks = 0;
        public HashMap<String, Long> executedPerTaskDefinition = new HashMap<>();

        public boolean hasTaskDefExecuted(String taskDefId) {
            return getTaskDefExecutedCount(taskDefId) != 0;
        }

        public long getTaskDefExecutedCount(String taskDefId) {
            return executedPerTaskDefinition.getOrDefault(taskDefId, 0L);
        }


        private void provideResource(Resource resource) {
            ++totalProvidedResources;
            providedResources.add(resource.getKey());
        }

        private void requireResource(Resource resource) {
            ++totalRequiredResources;
            requiredPerResource.merge(resource.getKey(), 1L, Long::sum);
        }

        private void requireTask(Task<?> task) {
            ++totalRequiredTasks;
            requiredPerTaskDefinition.merge(task.getId(), 1L, Long::sum);
        }

        private void checkProvidedResource(ResourceProvideDep dep) {
            ++totalCheckedProvidedResourceDependencies;
            checkedProvidedPerResource.merge(dep.key, 1L, Long::sum);
        }

        private void checkRequiredResource(ResourceRequireDep dep) {
            ++totalCheckedRequiredResourceDependencies;
            checkedRequiredPerResource.merge(dep.key, 1L, Long::sum);
        }

        private void checkRequiredTask(TaskRequireDep dep) {
            ++totalCheckedRequiredTaskDependencies;
            checkedRequiredPerTaskDefinition.merge(dep.callee.id, 1L, Long::sum);
        }

        private void executeTask(Task<?> task) {
            ++totalExecutedTasks;
            executedPerTaskDefinition.merge(task.getId(), 1L, Long::sum);
        }
    }

    private Report report = new Report();


    public void reset() {
        this.report = new Report();
    }

    public Report reportAndReset() {
        final Report report = this.report;
        this.report = new Report();
        return report;
    }


    @Override
    public void providedResource(Resource resource, ResourceStamper<?> stamper) {
        report.provideResource(resource);
    }

    @Override
    public void requiredResource(Resource resource, ResourceStamper<?> stamper) {
        report.requireResource(resource);
    }

    @Override
    public void requiredTask(Task<?> task, OutputStamper stamper) {
        report.requireTask(task);
    }


    @Override
    public void executeStart(TaskKey key, Task<?> task, ExecReason reason) {
        report.executeTask(task);
    }


    @Override
    public void checkResourceProvideStart(TaskKey provider, Task<?> task, ResourceProvideDep dep) {
        report.checkProvidedResource(dep);
    }

    @Override
    public void checkResourceRequireStart(TaskKey requirer, Task<?> task, ResourceRequireDep dep) {
        report.checkRequiredResource(dep);
    }

    @Override
    public void checkTaskRequireStart(TaskKey key, Task<?> task, TaskRequireDep dep) {
        report.checkRequiredTask(dep);
    }


    @Override
    public void checkAffectedByProvidedResource(TaskKey provider, @Nullable ResourceProvideDep dep, @Nullable InconsistentResourceProvide reason) {
        if(dep == null) return; // Provider is unobserved.
        report.checkProvidedResource(dep);
    }

    @Override
    public void checkAffectedByRequiredResource(TaskKey requirer, @Nullable ResourceRequireDep dep, @Nullable InconsistentResourceRequire reason) {
        if(dep == null) return; // Requirer is unobserved.
        report.checkRequiredResource(dep);
    }

    @Override
    public void checkAffectedByRequiredTask(TaskKey requirer, @Nullable TaskRequireDep dep, @Nullable InconsistentTaskRequire reason) {
        if(dep == null) return; // Requirer is unobserved.
        report.checkRequiredTask(dep);
    }
}
