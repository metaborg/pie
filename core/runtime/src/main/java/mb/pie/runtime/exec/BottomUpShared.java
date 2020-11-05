package mb.pie.runtime.exec;

import mb.pie.api.InconsistentResourceProvide;
import mb.pie.api.InconsistentResourceRequire;
import mb.pie.api.InconsistentTaskRequire;
import mb.pie.api.StoreReadTxn;
import mb.pie.api.TaskKey;
import mb.pie.api.TaskRequireDep;
import mb.pie.api.Tracer;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class BottomUpShared {
    /**
     * Notifies the {@code consumer} with tasks that are directly affected by requiring the {@code changedResource}.
     */
    public static void directlyAffectedByRequiredResource(
        StoreReadTxn txn,
        ResourceKey changedResource,
        ResourceService resourceService,
        Tracer tracer,
        Consumer<TaskKey> consumer
    ) {
        for(TaskKey requiree : txn.requireesOf(changedResource)) {
            final boolean isObserved = txn.taskObservability(requiree).isObserved();
            final @Nullable InconsistentResourceRequire reason;
            if(isObserved) {
                reason = txn.resourceRequires(requiree).stream()
                    .filter(dep -> dep.key.equals(changedResource))
                    .map(dep -> dep.checkConsistency(resourceService))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
            } else {
                reason = null;
            }
            tracer.scheduleAffectedByRequiredResource(changedResource, requiree, isObserved, reason);
            if(reason != null) {
                consumer.accept(requiree);
            }
        }
    }

    /**
     * Notifies the {@code consumer} with tasks that are directly affected by providing the {@code changedResource}.
     */
    public static void directlyAffectedByProvidedResource(
        StoreReadTxn txn,
        ResourceKey changedResource,
        ResourceService resourceService,
        Tracer tracer,
        Consumer<TaskKey> consumer
    ) {
        final @Nullable TaskKey provider = txn.providerOf(changedResource);
        if(provider != null) {
            final boolean isObserved = txn.taskObservability(provider).isObserved();
            final @Nullable InconsistentResourceProvide reason;
            if(isObserved) {
                reason = txn.resourceProvides(provider).stream()
                    .filter(dep -> dep.key.equals(changedResource))
                    .map(dep -> dep.checkConsistency(resourceService))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
            } else {
                reason = null;
            }
            tracer.scheduleAffectedByProvidedResource(changedResource, provider, isObserved, reason);
            if(reason != null) {
                consumer.accept(provider);
            }
        }
    }

    /**
     * Notifies the {@code consumer} with tasks that are directly affected by {@code changedResources}.
     */
    public static void directlyAffectedByResources(
        StoreReadTxn txn,
        Stream<? extends ResourceKey> changedResources,
        ResourceService resourceService,
        Tracer tracer,
        Consumer<TaskKey> consumer
    ) {
        changedResources.forEach((changedResource) -> {
            directlyAffectedByRequiredResource(txn, changedResource, resourceService, tracer, consumer);
            directlyAffectedByProvidedResource(txn, changedResource, resourceService, tracer, consumer);
        });
    }

    /**
     * Notifies the {@code consumer} with tasks that are directly affected by requiring the {@code requiree}.
     */
    public static void directlyAffectedByRequiredTask(
        StoreReadTxn txn,
        TaskKey requiree,
        @Nullable Serializable output,
        Tracer tracer,
        Consumer<TaskKey> consumer
    ) {
        for(TaskKey requirer : txn.callersOf(requiree)) {
            final boolean isObserved = txn.taskObservability(requirer).isObserved();
            final @Nullable InconsistentTaskRequire reason;
            if(isObserved) {
                reason = txn.taskRequires(requirer).stream()
                    .filter(dep -> dep.calleeEqual(requiree))
                    .map(dep -> dep.checkConsistency(output))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
            } else {
                reason = null;
            }
            tracer.scheduleAffectedByRequiredTask(requiree, requirer, isObserved, reason);
            if(reason != null) {
                consumer.accept(requirer);
            }
        }
    }

    /**
     * Checks whether [caller] has a transitive (or direct) task requirement to [callee].
     */
    public static boolean hasTransitiveTaskReq(StoreReadTxn txn, TaskKey caller, TaskKey callee) {
        // TODO: more efficient implementation for transitive calls?
        final Queue<TaskKey> toCheckQueue = new LinkedList<>();
        toCheckQueue.add(caller);
        while(!toCheckQueue.isEmpty()) {
            final TaskKey toCheck = toCheckQueue.poll();
            final List<TaskRequireDep> taskReqDeps = txn.taskRequires(toCheck);
            for(TaskRequireDep dep : taskReqDeps) {
                if(dep.calleeEqual(callee)) {
                    return true;
                }
                toCheckQueue.add(dep.callee);
            }
        }
        return false;
    }
}

