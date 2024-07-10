package mb.pie.api;

import mb.pie.api.stamp.ResourceStamp;
import mb.resource.Resource;
import mb.resource.ResourceKey;
import mb.resource.ResourceService;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;

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

    @Override public @Nullable InconsistentResourceRequire checkConsistency(ResourceService resourceService) {
        final Resource resource = resourceService.getResource(key);
        final ResourceStamp<Resource> newStamp;
        try {
            newStamp = stamp.getStamper().stamp(resource);
        } catch(IOException e) {
            throw new UncheckedIOException(e);
        }
        if(!stamp.equals(newStamp)) {
            return new InconsistentResourceRequire(this, newStamp);
        }
        return null;
    }

    @Override public boolean isConsistent(ResourceService resourceService) {
        final Resource resource = resourceService.getResource(key);
        final ResourceStamp<Resource> newStamp;
        try {
            newStamp = stamp.getStamper().stamp(resource);
        } catch(IOException e) {
            throw new UncheckedIOException(e);
        }
        return stamp.equals(newStamp);
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final ResourceRequireDep that = (ResourceRequireDep)o;
        if(!key.equals(that.key)) return false;
        return stamp.equals(that.stamp);
    }

    @Override public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + stamp.hashCode();
        return result;
    }

    @Override public String toString() {
        return "ResourceRequireDep(" + key + ", " + stamp + ")";
    }
}
