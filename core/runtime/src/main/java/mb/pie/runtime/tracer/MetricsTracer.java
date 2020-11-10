package mb.pie.runtime.tracer;

import mb.pie.api.InconsistentResourceProvide;
import mb.pie.api.InconsistentResourceRequire;
import mb.pie.api.InconsistentTaskRequire;
import mb.pie.api.ResourceProvideDep;
import mb.pie.api.ResourceRequireDep;
import mb.pie.api.Task;
import mb.pie.api.TaskData;
import mb.pie.api.TaskKey;
import mb.pie.api.TaskRequireDep;
import mb.pie.api.Tracer;
import mb.pie.api.exec.ExecReason;
import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamper;
import mb.resource.Resource;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;
import java.util.function.Consumer;

public class MetricsTracer implements Tracer {
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
    public void executeEndSuccess(TaskKey key, Task<?> task, ExecReason reason, TaskData data) {}

    @Override
    public void executeEndFailed(TaskKey key, Task<?> task, ExecReason reason, Exception e) {}

    @Override
    public void executeEndInterrupted(TaskKey key, Task<?> task, ExecReason reason, InterruptedException e) {}


    @Override
    public void upToDate(TaskKey key, Task<?> task) {}


    @Override
    public void requireTopDownInitialStart(TaskKey key, Task<?> task) {}

    @Override
    public void requireTopDownInitialEnd(TaskKey key, Task<?> task, @Nullable Serializable output) {}

    @Override
    public void checkTopDownStart(TaskKey key, Task<?> task) {}

    @Override
    public void checkTopDownEnd(TaskKey key, Task<?> task) {}

    @Override
    public void checkResourceProvideStart(TaskKey provider, Task<?> task, ResourceProvideDep dep) {
        ++report.checkedProvidedResourceDependencies;
    }

    @Override
    public void checkResourceProvideEnd(TaskKey provider, Task<?> task, ResourceProvideDep dep, @Nullable InconsistentResourceProvide reason) {}

    @Override
    public void checkResourceRequireStart(TaskKey requirer, Task<?> task, ResourceRequireDep dep) {
        ++report.checkedRequiredResourceDependencies;
    }

    @Override
    public void checkResourceRequireEnd(TaskKey requirer, Task<?> task, ResourceRequireDep dep, @Nullable InconsistentResourceRequire reason) {}

    @Override
    public void checkTaskRequireStart(TaskKey key, Task<?> task, TaskRequireDep dep) {
        ++report.checkedRequiredTaskDependencies;
    }

    @Override
    public void checkTaskRequireEnd(TaskKey key, Task<?> task, TaskRequireDep dep, @Nullable InconsistentTaskRequire reason) {}


    @Override
    public void requireBottomUpInitialStart(Set<? extends ResourceKey> changedResources) {}

    @Override
    public void requireBottomUpInitialEnd() {}

    @Override
    public void scheduleAffectedByResourceStart(ResourceKey resource) {}

    @Override
    public void checkAffectedByProvidedResource(TaskKey provider, @Nullable ResourceProvideDep dep, @Nullable InconsistentResourceProvide reason) {
        ++report.checkedProvidedResourceDependencies;
    }

    @Override
    public void checkAffectedByRequiredResource(TaskKey requirer, @Nullable ResourceRequireDep dep, @Nullable InconsistentResourceRequire reason) {
        ++report.checkedRequiredResourceDependencies;
    }

    @Override
    public void scheduleAffectedByResourceEnd(ResourceKey resource) {}

    @Override
    public void scheduleAffectedByTaskOutputStart(TaskKey requiree, @Nullable Serializable output) {}

    @Override
    public void checkAffectedByRequiredTask(TaskKey requirer, @Nullable TaskRequireDep dep, @Nullable InconsistentTaskRequire reason) {
        ++report.checkedRequiredTaskDependencies;
    }

    @Override
    public void scheduleAffectedByTaskOutputEnd(TaskKey requiree, @Nullable Serializable output) {}

    @Override
    public void scheduleTask(TaskKey key) {}

    @Override
    public void requireScheduledNowStart(TaskKey key) {}

    @Override
    public void requireScheduledNowEnd(TaskKey key, @Nullable TaskData data) {}


    @Override
    public void checkVisitedStart(TaskKey key) {}

    @Override
    public void checkVisitedEnd(TaskKey key, @Nullable Serializable output) {}

    @Override
    public void checkStoredStart(TaskKey key) {}

    @Override
    public void checkStoredEnd(TaskKey key, @Nullable Serializable output) {}

    @Override
    public void invokeCallbackStart(Consumer<@Nullable Serializable> observer, TaskKey key, @Nullable Serializable output) {}

    @Override
    public void invokeCallbackEnd(Consumer<@Nullable Serializable> observer, TaskKey key, @Nullable Serializable output) {}
}
