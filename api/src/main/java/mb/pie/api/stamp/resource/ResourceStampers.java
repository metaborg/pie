package mb.pie.api.stamp.resource;

import mb.resource.ReadableResource;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;

/**
 * Functions for creating common resource stampers.
 */
public class ResourceStampers {
    public static <R extends ReadableResource> ExistsResourceStamper<R> exists() {
        return new ExistsResourceStamper<>();
    }


    public static <R extends ReadableResource> ModifiedResourceStamper<R> modified() {
        return new ModifiedResourceStamper<>();
    }

    public static ModifiedMatchResourceStamper modified(ResourceMatcher matcher) {
        return new ModifiedMatchResourceStamper(matcher);
    }

    public static ModifiedWalkResourceStamper modified(ResourceWalker walker, ResourceMatcher matcher) {
        return new ModifiedWalkResourceStamper(walker, matcher);
    }


    public static <R extends ReadableResource> HashResourceStamper<R> hash() {
        return new HashResourceStamper<>();
    }

    public static HashMatchResourceStamper hash(ResourceMatcher matcher) {
        return new HashMatchResourceStamper(matcher);
    }

    public static HashWalkResourceStamper hash(ResourceWalker walker, ResourceMatcher matcher) {
        return new HashWalkResourceStamper(walker, matcher);
    }
}
