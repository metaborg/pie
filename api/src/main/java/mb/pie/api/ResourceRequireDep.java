package mb.pie.api;

import mb.pie.api.stamp.ResourceStamp;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

/**
 * Resource 'requires' (reads) dependency.
 */
public class ResourceRequireDep implements ResourceDep, Serializable {
    public final ResourceKey key;
    public final ResourceStamp<Resource> stamp;


    public ResourceRequireDep(ResourceKey key, ResourceStamp<Resource> stamp) {
        this.key = key;
        this.stamp = stamp;
    }


    @Override public @Nullable InconsistentResourceRequire checkConsistency(ResourceSystems systems) {
        final @Nullable ResourceSystem system = systems.getResourceSystem(key.getId());
        if(system == null) {
            throw new RuntimeException(
                "Cannot get resource system for resource key '" + key + "'; resource system with id '" + key.getId() + "' does not exist");
        }
        final Resource resource = system.getResource(key);
        final ResourceStamp<Resource> newStamp = stamp.getStamper().stamp(resource);
        if(stamp != newStamp) {
            return new InconsistentResourceRequire(this, newStamp);
        }
        return null;
    }

    @Override public Boolean isConsistent(ResourceSystems systems) {
        final @Nullable ResourceSystem system = systems.getResourceSystem(key.getId());
        if(system == null) {
            throw new RuntimeException(
                "Cannot get resource system for resource key '" + key + "'; resource system with id '" + key.getId() + "' does not exist");
        }
        final Resource resource = system.getResource(key);
        final ResourceStamp<Resource> newStamp = stamp.getStamper().stamp(resource);
        return stamp == newStamp;
    }

    @Override public String toString() {
        return "ResourceRequireDep(" + key + ", " + stamp + ")";
    }
}
