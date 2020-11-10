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
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;

public class MetricsTracer extends EmptyTracer {
    public static class Report {
        public long providedResources = 0;
        public long requiredResources = 0;
        public long requiredTasks = 0;

        public long checkedProvidedResourceDependencies = 0;
        public long checkedRequiredResourceDependencies = 0;
        public long checkedRequiredTaskDependencies = 0;

        public long executedTasks = 0;

        public HashMap<String, Long> requiredPerTaskDefinition = new HashMap<>();
        public HashMap<String, Long> executedPerTaskDefinition = new HashMap<>();

        private void requireTask(Task<?> task) {
            ++requiredTasks;
            requiredPerTaskDefinition.merge(task.getId(), 1L, Long::sum);
        }

        private void executeTask(Task<?> task) {
            ++executedTasks;
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
        ++report.providedResources;
    }

    @Override
    public void requiredResource(Resource resource, ResourceStamper<?> stamper) {
        ++report.requiredResources;
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
        ++report.checkedProvidedResourceDependencies;
    }

    @Override
    public void checkResourceRequireStart(TaskKey requirer, Task<?> task, ResourceRequireDep dep) {
        ++report.checkedRequiredResourceDependencies;
    }

    @Override
    public void checkTaskRequireStart(TaskKey key, Task<?> task, TaskRequireDep dep) {
        ++report.checkedRequiredTaskDependencies;
    }


    @Override
    public void checkAffectedByProvidedResource(TaskKey provider, @Nullable ResourceProvideDep dep, @Nullable InconsistentResourceProvide reason) {
        ++report.checkedProvidedResourceDependencies;
    }

    @Override
    public void checkAffectedByRequiredResource(TaskKey requirer, @Nullable ResourceRequireDep dep, @Nullable InconsistentResourceRequire reason) {
        ++report.checkedRequiredResourceDependencies;
    }

    @Override
    public void checkAffectedByRequiredTask(TaskKey requirer, @Nullable TaskRequireDep dep, @Nullable InconsistentTaskRequire reason) {
        ++report.checkedRequiredTaskDependencies;
    }
}
