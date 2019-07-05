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


    public static <R extends ReadableResource> ModifiedResourceStamper<R> modifiedFile() {
        return new ModifiedResourceStamper<>();
    }

    public static ModifiedMatchResourceStamper modifiedDir() {
        return new ModifiedMatchResourceStamper();
    }

    public static ModifiedMatchResourceStamper modifiedDir(ResourceMatcher matcher) {
        return new ModifiedMatchResourceStamper(matcher);
    }

    public static ModifiedWalkResourceStamper modifiedDirRec() {
        return new ModifiedWalkResourceStamper();
    }

    public static ModifiedWalkResourceStamper modifiedDirRec(ResourceWalker walker, ResourceMatcher matcher) {
        return new ModifiedWalkResourceStamper(walker, matcher);
    }


    public static <R extends ReadableResource> HashResourceStamper<R> hashFile() {
        return new HashResourceStamper<>();
    }

    public static HashMatchResourceStamper hashDir() {
        return new HashMatchResourceStamper();
    }

    public static HashMatchResourceStamper hashDir(ResourceMatcher matcher) {
        return new HashMatchResourceStamper(matcher);
    }

    public static HashWalkResourceStamper hashDirRec() {
        return new HashWalkResourceStamper();
    }

    public static HashWalkResourceStamper hashDirRec(ResourceWalker walker, ResourceMatcher matcher) {
        return new HashWalkResourceStamper(walker, matcher);
    }
}
