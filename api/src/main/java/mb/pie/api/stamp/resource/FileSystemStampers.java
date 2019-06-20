package mb.pie.api.stamp.resource;

import mb.resource.fs.FSResource;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;

/**
 * Common resource stampers for {@link FSResource}s.
 */
public class FileSystemStampers extends ReadableResourceStampers {
    public static ModifiedMatchResourceStamper modified(ResourceMatcher matcher) {
        return new ModifiedMatchResourceStamper(matcher);
    }

    public static ModifiedWalkResourceStamper modified(ResourceWalker walker, ResourceMatcher matcher) {
        return new ModifiedWalkResourceStamper(walker, matcher);
    }


    public static HashMatchResourceStamper hash(ResourceMatcher matcher) {
        return new HashMatchResourceStamper(matcher);
    }

    public static HashWalkResourceStamper hash(ResourceWalker walker, ResourceMatcher matcher) {
        return new HashWalkResourceStamper(walker, matcher);
    }
}
