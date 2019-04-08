package mb.pie.api.stamp.fs;

import mb.resource.fs.match.ResourceMatcher;
import mb.resource.fs.walk.ResourceWalker;

/**
 * Common file system stampers.
 */
public class FileSystemStampers {
    public static ExistsResourceStamper exists() {
        return new ExistsResourceStamper();
    }


    public static HashResourceStamper hash() {
        return new HashResourceStamper();
    }

    public static HashMatchResourceStamper hash(ResourceMatcher matcher) {
        return new HashMatchResourceStamper(matcher);
    }

    public static HashWalkResourceStamper hash(ResourceWalker walker, ResourceMatcher matcher) {
        return new HashWalkResourceStamper(walker, matcher);
    }


    public static ModifiedResourceStamper modified() {
        return new ModifiedResourceStamper();
    }

    public static ModifiedMatchResourceStamper modified(ResourceMatcher matcher) {
        return new ModifiedMatchResourceStamper(matcher);
    }

    public static ModifiedWalkResourceStamper modified(ResourceWalker walker, ResourceMatcher matcher) {
        return new ModifiedWalkResourceStamper(walker, matcher);
    }
}
