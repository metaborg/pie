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
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Set;
import java.util.function.Consumer;

public class NoopTracer implements Tracer {
    public static final NoopTracer instance = new NoopTracer();

    private NoopTracer() {}

    @Override public void requireTopDownInitialStart(TaskKey key, Task<?> task) {}

    @Override public void requireTopDownInitialEnd(TaskKey key, Task<?> task, @Nullable Serializable output) {}

    @Override public void requireTopDownStart(TaskKey key, Task<?> task) {}

    @Override public void requireTopDownEnd(TaskKey key, Task<?> task, @Nullable Serializable output) {}

    @Override public void requireBottomUpInitialStart(Set<? extends ResourceKey> changedResources) {}

    @Override public void requireBottomUpInitialEnd() {}

    @Override
    public void scheduleAffectedByProvidedResource(ResourceKey changedResource, TaskKey providee, boolean isObserved, @Nullable InconsistentResourceProvide reason) {}

    @Override
    public void scheduleAffectedByRequiredResource(ResourceKey changedResource, TaskKey requiree, boolean isObserved, @Nullable InconsistentResourceRequire reason) {}

    @Override
    public void scheduleAffectedByRequiredTask(TaskKey requiree, TaskKey requirer, boolean isObserved, @Nullable InconsistentTaskRequire reason) {}

    @Override public void scheduleTask(TaskKey key) {}

    @Override public void requireScheduledNowStart(TaskKey key) {}

    @Override public void requireScheduledNowEnd(TaskKey key, @Nullable TaskData data) {}

    @Override public void checkVisitedStart(TaskKey key) {}

    @Override public void checkVisitedEnd(TaskKey key, @Nullable Serializable output) {}

    @Override public void checkStoredStart(TaskKey key) {}

    @Override public void checkStoredEnd(TaskKey key, @Nullable Serializable output) {}

    @Override public void checkResourceProvideStart(TaskKey key, Task<?> task, ResourceProvideDep dep) {}

    @Override
    public void checkResourceProvideEnd(TaskKey key, Task<?> task, ResourceProvideDep dep, @Nullable InconsistentResourceProvide reason) {}

    @Override public void checkResourceRequireStart(TaskKey key, Task<?> task, ResourceRequireDep dep) {}

    @Override
    public void checkResourceRequireEnd(TaskKey key, Task<?> task, ResourceRequireDep dep, @Nullable InconsistentResourceRequire reason) {}

    @Override public void checkTaskRequireStart(TaskKey key, Task<?> task, TaskRequireDep dep) {}

    @Override
    public void checkTaskRequireEnd(TaskKey key, Task<?> task, TaskRequireDep dep, @Nullable InconsistentTaskRequire reason) {}

    @Override public void upToDate(TaskKey key, Task<?> task) {}

    @Override public void executeStart(TaskKey key, Task<?> task, ExecReason reason) {}

    @Override public void executeEndSuccess(TaskKey key, Task<?> task, ExecReason reason, TaskData data) {}

    @Override public void executeEndFailed(TaskKey key, Task<?> task, ExecReason reason, Exception e) {}

    @Override public void executeEndInterrupted(TaskKey key, Task<?> task, ExecReason reason, InterruptedException e) {}

    @Override
    public void invokeCallbackStart(@Nullable Consumer<Serializable> observer, TaskKey key, @Nullable Serializable output) {}

    @Override
    public void invokeCallbackEnd(@Nullable Consumer<Serializable> observer, TaskKey key, @Nullable Serializable output) {}
}
