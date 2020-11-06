package mb.pie.runtime.exec;

import mb.pie.api.InconsistentResourceProvide;
import mb.pie.api.InconsistentResourceRequire;
import mb.pie.api.InconsistentTaskRequire;
import mb.pie.api.ResourceProvideDep;
import mb.pie.api.ResourceRequireDep;
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
import java.util.Queue;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class BottomUpShared {
    /**
     * Notifies the {@code consumer} with tasks that are directly affected by providing the {@code changedResource}.
     */
    public static void directlyAffectedByProvidedResource(
        StoreReadTxn txn,
        ResourceKey resource,
        ResourceService resourceService,
        Tracer tracer,
        Consumer<TaskKey> consumer
    ) {
        final @Nullable TaskKey provider = txn.providerOf(resource);
        if(provider != null) {
            if(txn.taskObservability(provider).isObserved()) {
                for(ResourceProvideDep dep : txn.resourceProvides(provider)) {
                    if(!dep.key.equals(resource)) continue;
                    final @Nullable InconsistentResourceProvide reason = dep.checkConsistency(resourceService);
                    tracer.checkAffectedByProvidedResource(provider, dep, reason);
                    if(reason != null) {
                        consumer.accept(provider);
                        break;
                    }
                }
            } else {
                tracer.checkAffectedByProvidedResource(provider, null, null);
            }
        }
    }

    /**
     * Notifies the {@code consumer} with tasks that are directly affected by requiring the {@code resource}.
     */
    public static void directlyAffectedByRequiredResource(
        StoreReadTxn txn,
        ResourceKey resource,
        ResourceService resourceService,
        Tracer tracer,
        Consumer<TaskKey> consumer
    ) {
        for(TaskKey requirer : txn.requireesOf(resource)) {
            if(txn.taskObservability(requirer).isObserved()) {
                for(ResourceRequireDep dep : txn.resourceRequires(requirer)) {
                    if(!dep.key.equals(resource)) continue;
                    final @Nullable InconsistentResourceRequire reason = dep.checkConsistency(resourceService);
                    tracer.checkAffectedByRequiredResource(requirer, dep, reason);
                    if(reason != null) {
                        consumer.accept(requirer);
                        break;
                    }
                }
            } else {
                tracer.checkAffectedByRequiredResource(requirer, null, null);
            }
        }
    }

    /**
     * Notifies the {@code consumer} with tasks that are directly affected by {@code changedResources}.
     */
    public static void directlyAffectedByResources(
        StoreReadTxn txn,
        Stream<? extends ResourceKey> resources,
        ResourceService resourceService,
        Tracer tracer,
        Consumer<TaskKey> consumer
    ) {
        resources.forEach((resource) -> {
            tracer.scheduleAffectedByResourceStart(resource);
            directlyAffectedByProvidedResource(txn, resource, resourceService, tracer, consumer);
            directlyAffectedByRequiredResource(txn, resource, resourceService, tracer, consumer);
            tracer.scheduleAffectedByResourceEnd(resource);
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
        tracer.scheduleAffectedByTaskOutputStart(requiree, output);
        for(TaskKey requirer : txn.callersOf(requiree)) {
            if(txn.taskObservability(requirer).isObserved()) {
                for(TaskRequireDep dep : txn.taskRequires(requirer)) {
                    if(!dep.calleeEqual(requiree)) continue;
                    final @Nullable InconsistentTaskRequire reason = dep.checkConsistency(output);
                    tracer.checkAffectedByRequiredTask(requirer, dep, reason);
                    if(reason != null) {
                        consumer.accept(requirer);
                        break;
                    }
                }
            } else {
                tracer.checkAffectedByRequiredTask(requirer, null, null);
            }
        }
        tracer.scheduleAffectedByTaskOutputEnd(requiree, output);
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

