package mb.pie.api;

import mb.pie.api.exec.ExecReason;
import mb.resource.ResourceService;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Resource dependency that can be checked for consistency, given the collection of resource systems to resolve resource
 * keys into resources.
 */
public interface ResourceDep {
    /**
     * @return an execution reason when this resource dependency is inconsistent, `null` otherwise.
     */
    @Nullable ExecReason checkConsistency(ResourceService resourceService);

    /**
     * @return `true` when this resource dependency is consistent, `false` otherwise.
     */
    boolean isConsistent(ResourceService resourceService);
}
