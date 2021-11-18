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
import mb.pie.api.exec.ExecReason;
import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamper;
import mb.resource.Resource;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;

public class MetricsTracer extends EmptyTracer {
    public static class Report {
        public long totalProvidedResources = 0;
        public final HashSet<ResourceKey> providedResources = new HashSet<>();
        public long totalRequiredResources = 0;
        public final HashMap<ResourceKey, Long> requiredPerResource = new HashMap<>();
        public long totalRequiredTasks = 0;
        public final HashMap<String, Long> requiredPerTaskDefinition = new HashMap<>();

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


        public long totalCheckedProvidedResourceDependencyCount = 0;
        public final HashMap<ResourceKey, Long> checkedProvidedCountPerResource = new HashMap<>();
        public long totalCheckedRequiredResourceDependencyCount = 0;
        public final HashMap<ResourceKey, Long> checkedRequiredCountPerResource = new HashMap<>();
        public long totalCheckedRequiredTaskDependencyCount = 0;
        public final HashMap<String, Long> checkedRequiredCountPerTaskDefinition = new HashMap<>();

        public boolean hasResourceProvideDependencyBeenChecked(ResourceKey resourceKey) {
            return getResourceProvideDependencyCheckCount(resourceKey) != 0;
        }

        public long getResourceProvideDependencyCheckCount(ResourceKey resourceKey) {
            return checkedProvidedCountPerResource.getOrDefault(resourceKey, 0L);
        }

        public boolean hasResourceRequireDependencyBeenChecked(ResourceKey resourceKey) {
            return getResourceRequireDependencyCheckCount(resourceKey) != 0;
        }

        public long getResourceRequireDependencyCheckCount(ResourceKey resourceKey) {
            return checkedRequiredCountPerResource.getOrDefault(resourceKey, 0L);
        }

        public boolean hasTaskDefRequireDependencyBeenChecked(String taskDefId) {
            return getTaskDefRequireDependencyCheckCount(taskDefId) != 0;
        }

        public long getTaskDefRequireDependencyCheckCount(String taskDefId) {
            return requiredPerTaskDefinition.getOrDefault(taskDefId, 0L);
        }


        public long totalExecutionCount = 0;
        public long totalExecutionSuccessCount = 0;
        public long totalExecutionFailCount = 0;
        public long totalExecutionInterruptedCount = 0;
        public final HashMap<TaskKey, Long> executionCountPerTask = new HashMap<>();
        public final HashMap<String, Long> executionCountPerTaskDefinition = new HashMap<>();
        public final HashMap<TaskKey, Long> executionDurationPerTask = new HashMap<>();
        public final HashMap<String, Long> executionDurationPerTaskDefinition = new HashMap<>();
        private final Deque<TaskKey> executingTaskStack = new ArrayDeque<>();
        private final HashMap<TaskKey, Long> executionTimestampPerTask = new HashMap<>();

        public boolean hasTaskDefExecuted(String taskDefId) {
            return getTaskDefExecutionCount(taskDefId) != 0;
        }

        public long getTaskDefExecutionCount(String taskDefId) {
            return executionCountPerTaskDefinition.getOrDefault(taskDefId, 0L);
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
            ++totalCheckedProvidedResourceDependencyCount;
            checkedProvidedCountPerResource.merge(dep.key, 1L, Long::sum);
        }

        private void checkRequiredResource(ResourceRequireDep dep) {
            ++totalCheckedRequiredResourceDependencyCount;
            checkedRequiredCountPerResource.merge(dep.key, 1L, Long::sum);
        }

        private void checkRequiredTask(TaskRequireDep dep) {
            ++totalCheckedRequiredTaskDependencyCount;
            checkedRequiredCountPerTaskDefinition.merge(dep.callee.id, 1L, Long::sum);
        }


        private void executeTaskStart(TaskKey key) {
            assert !executionTimestampPerTask.containsKey(key) : "executionTimestampPerTask already contains timestamp for executing task";
            executionTimestampPerTask.put(key, System.nanoTime());
            executingTaskStack.push(key);
        }

        private void requireTaskStart() {
            final @Nullable TaskKey executingTask = executingTaskStack.peek();
            assert executingTask != null : "executingTask is null";
            storeDuration(executingTask);
        }

        private void requireTaskEnd() {
            final @Nullable TaskKey executingTask = executingTaskStack.peek();
            assert executingTask != null : "executingTask is null";
            assert !executionTimestampPerTask.containsKey(executingTask) : "executionTimestampPerTask already contains timestamp for executing task";
            executionTimestampPerTask.put(executingTask, System.nanoTime());
        }

        private void executeTaskEnd(TaskKey key) {
            assert key.equals(executingTaskStack.peek()) : "task in executeTaskEnd does not equal executingTask";
            storeDuration(key);
            executionCountPerTask.merge(key, 1L, Long::sum);
            executionCountPerTaskDefinition.merge(key.id, 1L, Long::sum);
            ++totalExecutionCount;
            executingTaskStack.pop();
        }

        private void executeTaskEndSuccess(TaskKey key) {
            executeTaskEnd(key);
            ++totalExecutionSuccessCount;
        }

        private void executeTaskEndFailed(TaskKey key) {
            executeTaskEnd(key);
            ++totalExecutionFailCount;
        }

        private void executeTaskEndInterrupted(TaskKey key) {
            executeTaskEnd(key);
            ++totalExecutionInterruptedCount;
        }

        private void storeDuration(TaskKey key) {
            final Long timestamp = executionTimestampPerTask.remove(key);
            assert timestamp != null : "timestamp is null";
            final Long duration = System.nanoTime() - timestamp;
            assert duration >= 0 : "duration is negative";
            executionDurationPerTask.merge(key, duration, Long::sum);
            executionDurationPerTaskDefinition.merge(key.id, duration, Long::sum);
        }
    }

    private Report report = new Report();


    Report getReport() {
        return report;
    }

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
        report.executeTaskStart(key);
    }

    @Override public void executeEndSuccess(TaskKey key, Task<?> task, ExecReason reason, TaskData data) {
        report.executeTaskEndSuccess(key);
    }

    @Override public void executeEndFailed(TaskKey key, Task<?> task, ExecReason reason, Exception e) {
        report.executeTaskEndFailed(key);
    }

    @Override public void executeEndInterrupted(TaskKey key, Task<?> task, ExecReason reason, InterruptedException e) {
        report.executeTaskEndInterrupted(key);
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
