package mb.pie.api.fs.stamp;

import mb.fs.api.node.match.FSNodeMatcher;
import mb.fs.api.node.walk.FSNodeWalker;

/**
 * Common file system stampers.
 */
public class FileSystemStampers {
    public static HashResourceStamper getHash() {
        return new HashResourceStamper();
    }

    public static HashResourceStamper hash(FSNodeMatcher matcher) {
        return new HashResourceStamper(matcher);
    }

    public static RecHashResourceStamper hash(FSNodeWalker walker, FSNodeMatcher matcher) {
        return new RecHashResourceStamper(walker, matcher);
    }


    public static ModifiedResourceStamper getModified() {
        return new ModifiedResourceStamper();
    }

    public static ModifiedResourceStamper modified(FSNodeMatcher matcher) {
        return new ModifiedResourceStamper(matcher);
    }

    public static RecModifiedResourceStamper modified(FSNodeWalker walker, FSNodeMatcher matcher) {
        return new RecModifiedResourceStamper(walker, matcher);
    }


    public static ExistsResourceStamper getExists() {
        return new ExistsResourceStamper();
    }
}
