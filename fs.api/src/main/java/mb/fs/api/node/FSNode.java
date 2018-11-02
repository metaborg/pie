package mb.fs.api.node;

import mb.fs.api.path.FSPath;
import mb.fs.api.path.RelativeFSPath;

import javax.annotation.Nullable;
import java.io.*;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public interface FSNode {
    FSPath getPath();


    @Nullable FSNode getParent();

    FSNode appendSegment(String segment);

    FSNode appendSegments(List<String> segments);

    FSNode appendSegments(Collection<String> segments);

    FSNode appendSegments(Iterable<String> segments);

    FSNode appendSegments(String... segments);

    FSNode appendRelativePath(RelativeFSPath relativePath);

    FSNode appendToLeafSegment(String str);

    FSNode replaceLeafSegment(String str);

    FSNode applyToLeafSegment(Function<String, String> func);


    FSNodeType getType() throws IOException;

    boolean isFile() throws IOException;

    boolean isDirectory() throws IOException;

    boolean exists() throws IOException;

    boolean isReadable() throws IOException;

    boolean isWritable() throws IOException;

    Instant getLastModifiedTime() throws IOException;

    void setLastModifiedTime(Instant time) throws IOException;

    long getSize() throws IOException;


    Stream<? extends FSNode> list() throws IOException;

    Stream<? extends FSNode> list(FSNodeMatcher matcher) throws IOException;


    Stream<? extends FSNode> walk() throws IOException;

    Stream<? extends FSNode> walk(FSNodeWalker walker, FSNodeMatcher matcher, @Nullable FSNodeAccess access) throws IOException;

    default Stream<? extends FSNode> walk(FSNodeWalker walker, FSNodeMatcher matcher) throws IOException {
        return walk(walker, matcher, null);
    }


    InputStream newInputStream() throws IOException;

    byte[] readAllBytes() throws IOException;


    OutputStream newOutputStream() throws IOException;


    void copyTo(FSNode other) throws IOException;

    void moveTo(FSNode other) throws IOException;


    void createFile(boolean createParents) throws IOException;

    default void createFile() throws IOException {
        createFile(false);
    }

    void createDirectory(boolean createParents) throws IOException;

    default void createDirectory() throws IOException {
        createDirectory(false);
    }

    void createParents() throws IOException;


    void delete(boolean deleteContents) throws IOException;

    default void delete() throws IOException {
        delete(false);
    }
}
