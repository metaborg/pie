package mb.pie.api;

import mb.pie.api.exec.ExecReason;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Logger interface for task executors.
 */
public interface ExecutorLogger {
    void requireTopDownInitialStart(TaskKey key, Task<?> task);

    void requireTopDownInitialEnd(TaskKey key, Task<?> task, @Nullable Serializable output);

    void requireTopDownStart(TaskKey key, Task<?> task);

    void requireTopDownEnd(TaskKey key, Task<?> task, @Nullable Serializable output);

    void requireBottomUpInitialStart(Set<ResourceKey> changedResources);

    void requireBottomUpInitialEnd();

    void checkVisitedStart(TaskKey key);

    void checkVisitedEnd(TaskKey key, @Nullable Serializable output);

    void checkStoredStart(TaskKey key);

    void checkStoredEnd(TaskKey key, @Nullable Serializable output);

    void checkResourceProvideStart(TaskKey key, Task<?> task, ResourceProvideDep dep);

    void checkResourceProvideEnd(TaskKey key, Task<?> task, ResourceProvideDep dep, @Nullable ExecReason reason);

    void checkResourceRequireStart(TaskKey key, Task<?> task, ResourceRequireDep dep);

    void checkResourceRequireEnd(TaskKey key, Task<?> task, ResourceRequireDep dep, @Nullable ExecReason reason);

    void checkTaskRequireStart(TaskKey key, Task<?> task, TaskRequireDep dep);

    void checkTaskRequireEnd(TaskKey key, Task<?> task, TaskRequireDep dep, @Nullable ExecReason reason);

    void executeStart(TaskKey key, Task<?> task, ExecReason reason);

    void executeEnd(TaskKey key, Task<?> task, ExecReason reason, TaskData data);

    void invokeCallbackStart(Consumer<@Nullable Serializable> observer, TaskKey key, @Nullable Serializable output);

    void invokeCallbackEnd(Consumer<@Nullable Serializable> observer, TaskKey key, @Nullable Serializable output);
}
