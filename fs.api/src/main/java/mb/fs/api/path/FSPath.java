package mb.fs.api.path;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;

/**
 * File system path, consisting of 0-* path segments.
 */
public interface FSPath extends Comparable<FSPath>, Serializable {
    String getFileSystemId();


    boolean isAbsolute();


    int getSegmentCount();

    Iterable<String> getSegments();


    @Nullable FSPath getParent();

    @Nullable FSPath getRoot();

    @Nullable String getLeaf();

    default @Nullable String getLeafExtension() {
        final @Nullable String leaf = getLeaf();
        if(leaf == null) {
            return null;
        }
        return FilenameExtensionUtil.extension(leaf);
    }

    FSPath getNormalized() throws FSPathNormalizationException;

    /**
     * @throws InvalidFSPathRuntimeException when `other` is not of the same runtime (super)type as this path.
     */
    FSPath relativize(FSPath other);


    /**
     * Creates a new path where given {@code segment} is appended to this path. Handling of the segment string is implementation-dependent.
     *
     * @param segment segment to append.
     * @return new path with {@code segment} appended.
     */
    FSPath appendSegment(String segment);

    FSPath appendSegments(Iterable<String> segments);

    FSPath appendSegments(Collection<String> segments);

    default FSPath appendSegments(List<String> segments) {
        return appendSegments((Collection<String>) segments);
    }

    default FSPath appendSegments(String... segments) {
        return appendSegments((Collection<String>) Arrays.asList(segments));
    }

    /**
     * @throws InvalidFSPathRuntimeException when `relativePath` is not of the same runtime (super)type as this path.
     * @throws InvalidFSPathRuntimeException when `relativePath` is not a relative path (but instead an absolute one).
     */
    FSPath appendRelativePath(FSPath relativePath);


    FSPath replaceLeaf(String segment);

    default FSPath appendToLeaf(String str) {
        return replaceLeaf(getLeaf() + str);
    }

    default FSPath applyToLeaf(Function<String, String> func) {
        return replaceLeaf(func.apply(getLeaf()));
    }

    default FSPath replaceLeafExtension(String extension) {
        final @Nullable String leaf = getLeaf();
        if(leaf == null) {
            return this;
        }
        return replaceLeaf(FilenameExtensionUtil.replaceExtension(leaf, extension));
    }

    default FSPath appendExtensionToLeaf(String extension) {
        final @Nullable String leaf = getLeaf();
        if(leaf == null) {
            return this;
        }
        return replaceLeaf(FilenameExtensionUtil.appendExtension(leaf, extension));
    }

    default FSPath applyToLeafExtension(Function<String, String> func) {
        final @Nullable String leaf = getLeaf();
        if(leaf == null) {
            return this;
        }
        return replaceLeaf(FilenameExtensionUtil.applyToExtension(leaf, func));
    }


    @Override boolean equals(Object other);

    @Override int hashCode();

    @Override String toString();


    /**
     * @throws InvalidFSPathRuntimeException when other is not of the same runtime (super)type as this path.
     */
    @Override int compareTo(FSPath other);
}
