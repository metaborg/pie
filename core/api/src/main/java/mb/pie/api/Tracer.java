package mb.pie.api;

import mb.pie.api.exec.ExecReason;
import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamper;
import mb.resource.Resource;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Interface for tracing build events, which can for example be used for debug logging or metrics collection.
 */
public interface Tracer {
    void providedResource(Resource resource, ResourceStamper<?> stamper);

    void requiredResource(Resource resource, ResourceStamper<?> stamper);

    void requiredTask(Task<?> task, OutputStamper stamper);


    void executeStart(TaskKey key, Task<?> task, ExecReason reason);

    void executeEndSuccess(TaskKey key, Task<?> task, ExecReason reason, TaskData data);

    void executeEndFailed(TaskKey key, Task<?> task, ExecReason reason, Exception e);

    void executeEndInterrupted(TaskKey key, Task<?> task, ExecReason reason, InterruptedException e);


    void upToDate(TaskKey key, Task<?> task);


    void requireTopDownInitialStart(TaskKey key, Task<?> task);

    void requireTopDownInitialEnd(TaskKey key, Task<?> task, @Nullable Serializable output);

    void checkTopDownStart(TaskKey key, Task<?> task);

    void checkTopDownEnd(TaskKey key, Task<?> task);

    void checkResourceProvideStart(TaskKey provider, Task<?> task, ResourceProvideDep dep);

    void checkResourceProvideEnd(TaskKey provider, Task<?> task, ResourceProvideDep dep, @Nullable InconsistentResourceProvide reason);

    void checkResourceRequireStart(TaskKey requirer, Task<?> task, ResourceRequireDep dep);

    void checkResourceRequireEnd(TaskKey requirer, Task<?> task, ResourceRequireDep dep, @Nullable InconsistentResourceRequire reason);

    void checkTaskRequireStart(TaskKey key, Task<?> task, TaskRequireDep dep);

    void checkTaskRequireEnd(TaskKey key, Task<?> task, TaskRequireDep dep, @Nullable InconsistentTaskRequire reason);


    void requireBottomUpInitialStart(Set<? extends ResourceKey> changedResources);

    void requireBottomUpInitialEnd();

    void scheduleAffectedByResourceStart(ResourceKey resource);

    void scheduleAffectedByResourceEnd(ResourceKey resource);

    void checkAffectedByProvidedResource(TaskKey provider, @Nullable ResourceProvideDep dep, @Nullable InconsistentResourceProvide reason);

    void checkAffectedByRequiredResource(TaskKey requirer, @Nullable ResourceRequireDep dep, @Nullable InconsistentResourceRequire reason);

    void scheduleAffectedByTaskOutputStart(TaskKey requiree, @Nullable Serializable output);

    void scheduleAffectedByTaskOutputEnd(TaskKey requiree, @Nullable Serializable output);

    void checkAffectedByRequiredTask(TaskKey requirer, @Nullable TaskRequireDep dep, @Nullable InconsistentTaskRequire reason);

    void scheduleTask(TaskKey key);

    void requireScheduledNowStart(TaskKey key);

    void requireScheduledNowEnd(TaskKey key, @Nullable TaskData data);


    void checkVisitedStart(TaskKey key);

    void checkVisitedEnd(TaskKey key, @Nullable Serializable output);

    void checkStoredStart(TaskKey key);

    void checkStoredEnd(TaskKey key, @Nullable Serializable output);

    void invokeCallbackStart(Consumer<@Nullable Serializable> observer, TaskKey key, @Nullable Serializable output);

    void invokeCallbackEnd(Consumer<@Nullable Serializable> observer, TaskKey key, @Nullable Serializable output);


    void setTaskObservability(TaskKey key, Observability previousObservability, Observability newObservability);
}
