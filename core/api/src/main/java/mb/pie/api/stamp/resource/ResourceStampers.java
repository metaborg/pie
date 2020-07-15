package mb.pie.api.stamp.resource;

import mb.resource.ReadableResource;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;

/**
 * Common resource stampers.
 */
public class ResourceStampers {
    /**
     * Returns a stamper for {@link ReadableResource readable resources} that stamps based on whether the resource
     * exists or not.
     *
     * @param <R> Type of {@link ReadableResource readable resource}.
     * @return Stamper.
     */
    public static <R extends ReadableResource> ExistsResourceStamper<R> exists() {
        return new ExistsResourceStamper<>();
    }


    /**
     * Returns a stamper for {@link ReadableResource readable resources} that stamps based on the last modification date
     * of the resource. Directories can be stamped with this stamper, but the stamp will only change when a resource
     * inside the directory is added or removed, not when the last modification date of a resource inside the directory
     * is changed.
     *
     * @param <R> Type of {@link ReadableResource readable resource}.
     * @return Stamper.
     */
    public static <R extends ReadableResource> ModifiedResourceStamper<R> modifiedFile() {
        return new ModifiedResourceStamper<>();
    }

    /**
     * Returns a stamper for {@link HierarchicalResource hierarchical resources} that stamps based on the modification
     * date of a file, or on the maximum modification date of non-recursive resources in a directory.
     *
     * @return Stamper.
     */
    public static ModifiedMatchResourceStamper modifiedDir() {
        return new ModifiedMatchResourceStamper();
    }

    /**
     * Returns a stamper for {@link HierarchicalResource hierarchical resources} that stamps based on the modification
     * date of a file, or on the maximum modification date of non-recursive resources in a directory. When the resource
     * is a directory, only stamps resources in the directory that match given {@link ResourceMatcher resource
     * matcher}.
     *
     * @param matcher Resource matcher.
     * @return Stamper.
     */
    public static ModifiedMatchResourceStamper modifiedDir(ResourceMatcher matcher) {
        return new ModifiedMatchResourceStamper(matcher);
    }

    /**
     * Returns a stamper for {@link HierarchicalResource hierarchical resources} that stamps based on the modification
     * date of a file, or on the maximum modification date of recursive resources in a directory.
     *
     * @return Stamper.
     */
    public static ModifiedWalkResourceStamper modifiedDirRec() {
        return new ModifiedWalkResourceStamper();
    }

    /**
     * Returns a stamper for {@link HierarchicalResource hierarchical resources} that stamps based on the modification
     * date of a file, or on the maximum modification date of recursive resources in a directory. When the resource is a
     * directory, only traverses subdirectories that match given {@link ResourceWalker resource walker}, and only stamps
     * files that match given {@link ResourceMatcher resource matcher}.
     *
     * @param matcher Resource matcher.
     * @param walker  Resource walker.
     * @return Stamper.
     */
    public static ModifiedWalkResourceStamper modifiedDirRec(ResourceWalker walker, ResourceMatcher matcher) {
        return new ModifiedWalkResourceStamper(walker, matcher);
    }


    /**
     * Returns a stamper for {@link ReadableResource readable resources} that stamps based on the hash of the contents
     * of of the resource. Directories cannot be stamped with this stamper.
     *
     * @param <R> Type of {@link ReadableResource readable resource}.
     * @return Stamper.
     */
    public static <R extends ReadableResource> HashResourceStamper<R> hashFile() {
        return new HashResourceStamper<>();
    }

    /**
     * Returns a stamper for {@link HierarchicalResource hierarchical resources} that stamps based on the hash of the
     * contents of a file, or on the combined hash of non-recursive resources in a directory.
     *
     * @return Stamper.
     */
    public static HashMatchResourceStamper hashDir() {
        return new HashMatchResourceStamper();
    }

    /**
     * Returns a stamper for {@link HierarchicalResource hierarchical resources} that stamps based on the hash of the
     * contents of a file, or on the combined hash of non-recursive resources in a directory. When the resource is a
     * directory, only hashes resources in the directory that match given {@link ResourceMatcher resource matcher}.
     *
     * @param matcher Resource matcher.
     * @return Stamper.
     */
    public static HashMatchResourceStamper hashDir(ResourceMatcher matcher) {
        return new HashMatchResourceStamper(matcher);
    }

    /**
     * Returns a stamper for {@link HierarchicalResource hierarchical resources} that stamps based on the hash of the
     * contents of a file, or on the combined hash of recursive resources in a directory.
     *
     * @return Stamper.
     */
    public static HashWalkResourceStamper hashDirRec() {
        return new HashWalkResourceStamper();
    }

    /**
     * Returns a stamper for {@link HierarchicalResource hierarchical resources} that stamps based on the hash of the
     * contents of a file, or on the combined hash of recursive resources in a directory. When the resource is a
     * directory, only traverses subdirectories that match given {@link ResourceWalker resource walker}, and only hashes
     * files that match given {@link ResourceMatcher resource matcher}.
     *
     * @param matcher Resource matcher.
     * @param walker  Resource walker.
     * @return Stamper.
     */
    public static HashWalkResourceStamper hashDirRec(ResourceWalker walker, ResourceMatcher matcher) {
        return new HashWalkResourceStamper(walker, matcher);
    }
}
