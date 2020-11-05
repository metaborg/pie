package mb.pie.api;

import mb.pie.api.exec.ExecReason;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Interface for tracing build events, which can for example be used for debug logging or metrics collection.
 */
public interface Tracer {
    void requireTopDownInitialStart(TaskKey key, Task<?> task);

    void requireTopDownInitialEnd(TaskKey key, Task<?> task, @Nullable Serializable output);

    void requireTopDownStart(TaskKey key, Task<?> task);

    void requireTopDownEnd(TaskKey key, Task<?> task, @Nullable Serializable output);


    void requireBottomUpInitialStart(Set<? extends ResourceKey> changedResources);

    void requireBottomUpInitialEnd();

    void scheduleAffectedByProvidedResource(ResourceKey changedResource, TaskKey providee, boolean isObserved, @Nullable InconsistentResourceProvide reason);

    void scheduleAffectedByRequiredResource(ResourceKey changedResource, TaskKey requiree, boolean isObserved, @Nullable InconsistentResourceRequire reason);

    void scheduleAffectedByRequiredTask(TaskKey requiree, TaskKey requirer, boolean isObserved, @Nullable InconsistentTaskRequire reason);

    void scheduleTask(TaskKey key);

    void requireScheduledNowStart(TaskKey key);

    void requireScheduledNowEnd(TaskKey key, @Nullable TaskData data);


    void checkVisitedStart(TaskKey key);

    void checkVisitedEnd(TaskKey key, @Nullable Serializable output);

    void checkStoredStart(TaskKey key);

    void checkStoredEnd(TaskKey key, @Nullable Serializable output);

    void checkResourceProvideStart(TaskKey key, Task<?> task, ResourceProvideDep dep);

    void checkResourceProvideEnd(TaskKey key, Task<?> task, ResourceProvideDep dep, @Nullable InconsistentResourceProvide reason);

    void checkResourceRequireStart(TaskKey key, Task<?> task, ResourceRequireDep dep);

    void checkResourceRequireEnd(TaskKey key, Task<?> task, ResourceRequireDep dep, @Nullable InconsistentResourceRequire reason);

    void checkTaskRequireStart(TaskKey key, Task<?> task, TaskRequireDep dep);

    void checkTaskRequireEnd(TaskKey key, Task<?> task, TaskRequireDep dep, @Nullable InconsistentTaskRequire reason);


    void upToDate(TaskKey key, Task<?> task);

    void executeStart(TaskKey key, Task<?> task, ExecReason reason);

    void executeEndSuccess(TaskKey key, Task<?> task, ExecReason reason, TaskData data);

    void executeEndFailed(TaskKey key, Task<?> task, ExecReason reason, Exception e);

    void executeEndInterrupted(TaskKey key, Task<?> task, ExecReason reason, InterruptedException e);


    void invokeCallbackStart(Consumer<@Nullable Serializable> observer, TaskKey key, @Nullable Serializable output);

    void invokeCallbackEnd(Consumer<@Nullable Serializable> observer, TaskKey key, @Nullable Serializable output);
}
