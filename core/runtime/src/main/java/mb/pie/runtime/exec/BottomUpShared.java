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
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class BottomUpShared {
    /**
     * Notifies the {@code consumer} with tasks that are directly affected by providing the {@code changedResource}.
     */
    public static void directlyAffectedByProvidedResource(
        ResourceKey resource,
        ResourceService resourceService,
        StoreReadTxn txn,
        Tracer tracer,
        Consumer<TaskKey> consumer
    ) {
        final @Nullable TaskKey provider = txn.getProviderOf(resource);
        if(provider != null) {
            if(txn.getTaskObservability(provider).isObserved()) {
                for(ResourceProvideDep dep : txn.getResourceProvideDeps(provider)) {
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
        ResourceKey resource,
        ResourceService resourceService,
        StoreReadTxn txn,
        Tracer tracer,
        Consumer<TaskKey> consumer
    ) {
        for(TaskKey requirer : txn.getRequirersOf(resource)) {
            if(txn.getTaskObservability(requirer).isObserved()) {
                for(ResourceRequireDep dep : txn.getResourceRequireDeps(requirer)) {
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
        Stream<? extends ResourceKey> resources,
        ResourceService resourceService,
        StoreReadTxn txn,
        Tracer tracer,
        Consumer<TaskKey> consumer
    ) {
        resources.forEach((resource) -> {
            tracer.scheduleAffectedByResourceStart(resource);
            directlyAffectedByProvidedResource(resource, resourceService, txn, tracer, consumer);
            directlyAffectedByRequiredResource(resource, resourceService, txn, tracer, consumer);
            tracer.scheduleAffectedByResourceEnd(resource);
        });
    }

    /**
     * Notifies the {@code consumer} with tasks that are directly affected by requiring the {@code requiree}.
     */
    public static void directlyAffectedByRequiredTask(
        TaskKey requiree,
        @Nullable Serializable output,
        StoreReadTxn txn,
        Tracer tracer,
        Consumer<TaskKey> consumer
    ) {
        tracer.scheduleAffectedByTaskOutputStart(requiree, output);
        for(TaskKey requirer : txn.getCallersOf(requiree)) {
            if(txn.getTaskObservability(requirer).isObserved()) {
                for(TaskRequireDep dep : txn.getTaskRequireDeps(requirer)) {
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
    public static boolean hasTransitiveTaskReq(TaskKey caller, TaskKey callee, StoreReadTxn txn) {
        // TODO: more efficient implementation for transitive calls?
        final Queue<TaskKey> toCheckQueue = new LinkedList<>();
        toCheckQueue.add(caller);
        while(!toCheckQueue.isEmpty()) {
            final TaskKey toCheck = toCheckQueue.poll();
            final Collection<TaskKey> requiredTasks = txn.getRequiredTasks(toCheck);
            for(TaskKey requiredTask : requiredTasks) {
                if(requiredTask.equals(callee)) {
                    return true;
                }
                toCheckQueue.add(requiredTask);
            }
        }
        return false;
    }
}

