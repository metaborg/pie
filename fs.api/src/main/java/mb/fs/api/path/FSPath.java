package mb.fs.api.path;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;

public interface FSPath extends Comparable<FSPath>, Serializable {
    String getFileSystemId();


    boolean isAbsolute();


    int getSegmentCount();

    Iterable<String> getSegments();


    @Nullable FSPath getParent();

    @Nullable FSPath getRoot();

    @Nullable String getLeaf();

    FSPath getNormalized() throws FSPathNormalizationException;

    /**
     * @throws InvalidFSPathRuntimeException when absolutePath is not of the same runtime (super)type as this path.
     * @throws InvalidFSPathRuntimeException when absolutePath is not an absolute path (but instead a relative one).
     */
    FSPath getRelativeTo(FSPath absolutePath);


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
     * @throws InvalidFSPathRuntimeException when relativePath is not of the same runtime (super)type as this path.
     * @throws InvalidFSPathRuntimeException when relativePath is not a relative path (but instead an absolute one).
     */
    FSPath appendRelativePath(FSPath relativePath);


    FSPath replaceLeafSegment(String segment);

    default FSPath appendToLeafSegment(String str) {
        return replaceLeafSegment(getLeaf() + str);
    }

    default FSPath applyToLeafSegment(Function<String, String> func) {
        return replaceLeafSegment(func.apply(getLeaf()));
    }


    @Override boolean equals(Object other);

    @Override int hashCode();

    @Override String toString();


    /**
     * @throws InvalidFSPathRuntimeException when other is not of the same runtime (super)type as this path.
     */
    @Override int compareTo(FSPath other);
}
