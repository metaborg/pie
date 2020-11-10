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
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class CompositeTracer implements Tracer {
    private final List<Tracer> tracers;


    public CompositeTracer(List<Tracer> tracers) {
        this.tracers = tracers;
    }

    public CompositeTracer(Tracer... tracers) {
        this(Arrays.asList(tracers));
    }


    @Override
    public void providedResource(Resource resource, ResourceStamper<?> stamper) {
        for(Tracer tracer : tracers) {
            tracer.providedResource(resource, stamper);
        }
    }

    @Override
    public void requiredResource(Resource resource, ResourceStamper<?> stamper) {
        for(Tracer tracer : tracers) {
            tracer.requiredResource(resource, stamper);
        }
    }

    @Override
    public void requiredTask(Task<?> task, OutputStamper stamper) {
        for(Tracer tracer : tracers) {
            tracer.requiredTask(task, stamper);
        }
    }


    @Override
    public void executeStart(TaskKey key, Task<?> task, ExecReason reason) {
        for(Tracer tracer : tracers) {
            tracer.executeStart(key, task, reason);
        }
    }

    @Override
    public void executeEndSuccess(TaskKey key, Task<?> task, ExecReason reason, TaskData data) {
        for(Tracer tracer : tracers) {
            tracer.executeEndSuccess(key, task, reason, data);
        }
    }

    @Override
    public void executeEndFailed(TaskKey key, Task<?> task, ExecReason reason, Exception e) {
        for(Tracer tracer : tracers) {
            tracer.executeEndFailed(key, task, reason, e);
        }
    }

    @Override
    public void executeEndInterrupted(TaskKey key, Task<?> task, ExecReason reason, InterruptedException e) {
        for(Tracer tracer : tracers) {
            tracer.executeEndInterrupted(key, task, reason, e);
        }
    }


    @Override
    public void upToDate(TaskKey key, Task<?> task) {
        for(Tracer tracer : tracers) {
            tracer.upToDate(key, task);
        }
    }


    @Override
    public void requireTopDownInitialStart(TaskKey key, Task<?> task) {
        for(Tracer tracer : tracers) {
            tracer.requireTopDownInitialStart(key, task);
        }
    }

    @Override
    public void requireTopDownInitialEnd(TaskKey key, Task<?> task, @Nullable Serializable output) {
        for(Tracer tracer : tracers) {
            tracer.requireTopDownInitialEnd(key, task, output);
        }
    }

    @Override
    public void checkTopDownStart(TaskKey key, Task<?> task) {
        for(Tracer tracer : tracers) {
            tracer.checkTopDownStart(key, task);
        }
    }

    @Override
    public void checkTopDownEnd(TaskKey key, Task<?> task) {
        for(Tracer tracer : tracers) {
            tracer.checkTopDownEnd(key, task);
        }
    }

    @Override
    public void checkResourceProvideStart(TaskKey provider, Task<?> task, ResourceProvideDep dep) {
        for(Tracer tracer : tracers) {
            tracer.checkResourceProvideStart(provider, task, dep);
        }
    }

    @Override
    public void checkResourceProvideEnd(TaskKey provider, Task<?> task, ResourceProvideDep dep, @Nullable InconsistentResourceProvide reason) {
        for(Tracer tracer : tracers) {
            tracer.checkResourceProvideEnd(provider, task, dep, reason);
        }
    }

    @Override
    public void checkResourceRequireStart(TaskKey requirer, Task<?> task, ResourceRequireDep dep) {
        for(Tracer tracer : tracers) {
            tracer.checkResourceRequireStart(requirer, task, dep);
        }
    }

    @Override
    public void checkResourceRequireEnd(TaskKey requirer, Task<?> task, ResourceRequireDep dep, @Nullable InconsistentResourceRequire reason) {
        for(Tracer tracer : tracers) {
            tracer.checkResourceRequireEnd(requirer, task, dep, reason);
        }
    }

    @Override
    public void checkTaskRequireStart(TaskKey key, Task<?> task, TaskRequireDep dep) {
        for(Tracer tracer : tracers) {
            tracer.checkTaskRequireStart(key, task, dep);
        }
    }

    @Override
    public void checkTaskRequireEnd(TaskKey key, Task<?> task, TaskRequireDep dep, @Nullable InconsistentTaskRequire reason) {
        for(Tracer tracer : tracers) {
            tracer.checkTaskRequireEnd(key, task, dep, reason);
        }
    }


    @Override
    public void requireBottomUpInitialStart(Set<? extends ResourceKey> changedResources) {
        for(Tracer tracer : tracers) {
            tracer.requireBottomUpInitialStart(changedResources);
        }
    }

    @Override
    public void requireBottomUpInitialEnd() {}

    @Override
    public void scheduleAffectedByResourceStart(ResourceKey resource) {
        for(Tracer tracer : tracers) {
            tracer.scheduleAffectedByResourceStart(resource);
        }
    }

    @Override
    public void scheduleAffectedByResourceEnd(ResourceKey resource) {
        for(Tracer tracer : tracers) {
            tracer.scheduleAffectedByResourceEnd(resource);
        }
    }

    @Override
    public void checkAffectedByProvidedResource(TaskKey provider, @Nullable ResourceProvideDep dep, @Nullable InconsistentResourceProvide reason) {
        for(Tracer tracer : tracers) {
            tracer.checkAffectedByProvidedResource(provider, dep, reason);
        }
    }

    @Override
    public void checkAffectedByRequiredResource(TaskKey requirer, @Nullable ResourceRequireDep dep, @Nullable InconsistentResourceRequire reason) {
        for(Tracer tracer : tracers) {
            tracer.checkAffectedByRequiredResource(requirer, dep, reason);
        }
    }

    @Override
    public void scheduleAffectedByTaskOutputStart(TaskKey requiree, @Nullable Serializable output) {
        for(Tracer tracer : tracers) {
            tracer.scheduleAffectedByTaskOutputStart(requiree, output);
        }
    }

    @Override
    public void scheduleAffectedByTaskOutputEnd(TaskKey requiree, @Nullable Serializable output) {
        for(Tracer tracer : tracers) {
            tracer.scheduleAffectedByTaskOutputEnd(requiree, output);
        }
    }

    @Override
    public void checkAffectedByRequiredTask(TaskKey requirer, @Nullable TaskRequireDep dep, @Nullable InconsistentTaskRequire reason) {
        for(Tracer tracer : tracers) {
            tracer.checkAffectedByRequiredTask(requirer, dep, reason);
        }
    }

    @Override
    public void scheduleTask(TaskKey key) {
        for(Tracer tracer : tracers) {
            tracer.scheduleTask(key);
        }
    }

    @Override
    public void requireScheduledNowStart(TaskKey key) {
        for(Tracer tracer : tracers) {
            tracer.requireScheduledNowStart(key);
        }
    }

    @Override
    public void requireScheduledNowEnd(TaskKey key, @Nullable TaskData data) {
        for(Tracer tracer : tracers) {
            tracer.requireScheduledNowEnd(key, data);
        }
    }


    @Override
    public void checkVisitedStart(TaskKey key) {
        for(Tracer tracer : tracers) {
            tracer.checkVisitedStart(key);
        }
    }

    @Override
    public void checkVisitedEnd(TaskKey key, @Nullable Serializable output) {
        for(Tracer tracer : tracers) {
            tracer.checkVisitedEnd(key, output);
        }
    }

    @Override
    public void checkStoredStart(TaskKey key) {
        for(Tracer tracer : tracers) {
            tracer.checkStoredStart(key);
        }
    }

    @Override
    public void checkStoredEnd(TaskKey key, @Nullable Serializable output) {
        for(Tracer tracer : tracers) {
            tracer.checkStoredEnd(key, output);
        }
    }

    @Override
    public void invokeCallbackStart(Consumer<@Nullable Serializable> observer, TaskKey key, @Nullable Serializable output) {
        for(Tracer tracer : tracers) {
            tracer.invokeCallbackStart(observer, key, output);
        }
    }

    @Override
    public void invokeCallbackEnd(Consumer<@Nullable Serializable> observer, TaskKey key, @Nullable Serializable output) {
        for(Tracer tracer : tracers) {
            tracer.invokeCallbackEnd(observer, key, output);
        }
    }
}
