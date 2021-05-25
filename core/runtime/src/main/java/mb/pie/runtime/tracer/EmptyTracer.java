package mb.pie.runtime.tracer;

import mb.pie.api.InconsistentResourceProvide;
import mb.pie.api.InconsistentResourceRequire;
import mb.pie.api.InconsistentTaskRequire;
import mb.pie.api.Observability;
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
import java.util.Set;
import java.util.function.Consumer;

class EmptyTracer implements Tracer {
    @Override
    public void providedResource(Resource resource, ResourceStamper<?> stamper) {}

    @Override
    public void requiredResource(Resource resource, ResourceStamper<?> stamper) {}

    @Override
    public void requiredTask(Task<?> task, OutputStamper stamper) {}


    @Override
    public void executeStart(TaskKey key, Task<?> task, ExecReason reason) {}

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
    public void checkResourceProvideStart(TaskKey provider, Task<?> task, ResourceProvideDep dep) {}

    @Override
    public void checkResourceProvideEnd(TaskKey provider, Task<?> task, ResourceProvideDep dep, @Nullable InconsistentResourceProvide reason) {}

    @Override
    public void checkResourceRequireStart(TaskKey requirer, Task<?> task, ResourceRequireDep dep) {}

    @Override
    public void checkResourceRequireEnd(TaskKey requirer, Task<?> task, ResourceRequireDep dep, @Nullable InconsistentResourceRequire reason) {}

    @Override
    public void checkTaskRequireStart(TaskKey key, Task<?> task, TaskRequireDep dep) {}

    @Override
    public void checkTaskRequireEnd(TaskKey key, Task<?> task, TaskRequireDep dep, @Nullable InconsistentTaskRequire reason) {}


    @Override
    public void requireBottomUpInitialStart(Set<? extends ResourceKey> changedResources) {}

    @Override
    public void requireBottomUpInitialEnd() {}

    @Override
    public void scheduleAffectedByResourceStart(ResourceKey resource) {}

    @Override
    public void scheduleAffectedByResourceEnd(ResourceKey resource) {}

    @Override
    public void checkAffectedByProvidedResource(TaskKey provider, @Nullable ResourceProvideDep dep, @Nullable InconsistentResourceProvide reason) {}

    @Override
    public void checkAffectedByRequiredResource(TaskKey requirer, @Nullable ResourceRequireDep dep, @Nullable InconsistentResourceRequire reason) {}

    @Override
    public void scheduleAffectedByTaskOutputStart(TaskKey requiree, @Nullable Serializable output) {}

    @Override
    public void scheduleAffectedByTaskOutputEnd(TaskKey requiree, @Nullable Serializable output) {}

    @Override
    public void checkAffectedByRequiredTask(TaskKey requirer, @Nullable TaskRequireDep dep, @Nullable InconsistentTaskRequire reason) {}

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
    public void invokeCallbackStart(@Nullable Consumer<Serializable> observer, TaskKey key, @Nullable Serializable output) {}

    @Override
    public void invokeCallbackEnd(@Nullable Consumer<Serializable> observer, TaskKey key, @Nullable Serializable output) {}


    @Override
    public void setTaskObservability(TaskKey key, Observability previousObservability, Observability newObservability) {}
}
