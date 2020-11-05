package mb.pie.runtime.exec;

import mb.log.api.Logger;
import mb.pie.api.StoreReadTxn;
import mb.pie.api.TaskKey;
import mb.pie.api.TaskRequireDep;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.LinkedList;
import java.util.List;
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
        Logger logger,
        Consumer<TaskKey> consumer
    ) {
        for(TaskKey requiree : txn.requireesOf(changedResource)) {
            logger.trace("  - required by: " + requiree.toShortString(200));
            if(txn.taskObservability(requiree).isUnobserved()) {
                logger.trace("    @ is unobserved; skipping");
            } else if(!txn.resourceRequires(requiree).stream().filter(dep -> dep.key.equals(changedResource)).allMatch(dep -> dep.isConsistent(resourceService))) {
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
        Logger logger,
        Consumer<TaskKey> consumer
    ) {
        final @Nullable TaskKey provider = txn.providerOf(changedResource);
        if(provider != null) {
            logger.trace("  - provided by: " + provider.toShortString(200));
            if(txn.taskObservability(provider).isUnobserved()) {
                logger.trace("    @ is unobserved; skipping");
            } else if(!txn.resourceProvides(provider).stream().filter(dep -> dep.key.equals(changedResource)).allMatch(dep -> dep.isConsistent(resourceService))) {
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
        Logger logger,
        Consumer<TaskKey> consumer
    ) {
        changedResources.forEach((changedResource) -> {
            logger.trace("* resource: " + changedResource);
            directlyAffectedByRequiredResource(txn, changedResource, resourceService, logger, consumer);
            directlyAffectedByProvidedResource(txn, changedResource, resourceService, logger, consumer);
        });
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

