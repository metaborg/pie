package mb.fs.api.node;

import mb.fs.api.FileSystem;
import mb.fs.api.node.match.FSNodeMatcher;
import mb.fs.api.node.walk.FSNodeWalker;
import mb.fs.api.path.*;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public interface FSNode {
    FSPath getPath();

    FileSystem getFileSystem();

    String getFileSystemId();


    @Nullable FSNode getParent();

    @Nullable FSNode getRoot();

    @Nullable String getLeaf();

    default @Nullable String getLeafExtension() {
        final @Nullable String leaf = getLeaf();
        if(leaf == null) {
            return null;
        }
        return FilenameExtensionUtil.extension(leaf);
    }


    FSNode appendSegment(String segment);

    FSNode appendSegments(Iterable<String> segments);

    FSNode appendSegments(Collection<String> segments);

    default FSNode appendSegments(List<String> segments) {
        return appendSegments((Collection<String>) segments);
    }

    default FSNode appendSegments(String... segments) {
        return appendSegments((Collection<String>) Arrays.asList(segments));
    }

    /**
     * @throws InvalidFSPathRuntimeException when relativePath is not of the same runtime (super)type as the path from {@link #getPath}.
     * @throws InvalidFSPathRuntimeException when relativePath is not a relative path (but instead an absolute one).
     */
    FSNode appendRelativePath(FSPath relativePath);


    FSNode replaceLeaf(String segment);

    default FSNode appendToLeaf(String str) {
        return replaceLeaf(getLeaf() + str);
    }

    default FSNode applyToLeaf(Function<String, String> func) {
        return replaceLeaf(func.apply(getLeaf()));
    }

    default FSNode replaceLeafExtension(String extension) {
        final @Nullable String leaf = getLeaf();
        if(leaf == null) {
            return this;
        }
        return replaceLeaf(FilenameExtensionUtil.replaceExtension(leaf, extension));
    }

    default FSNode appendExtensionToLeaf(String extension) {
        final @Nullable String leaf = getLeaf();
        if(leaf == null) {
            return this;
        }
        return replaceLeaf(FilenameExtensionUtil.appendExtension(leaf, extension));
    }

    default FSNode applyToLeafExtension(Function<String, String> func) {
        final @Nullable String leaf = getLeaf();
        if(leaf == null) {
            return this;
        }
        return replaceLeaf(FilenameExtensionUtil.applyToExtension(leaf, func));
    }


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

    List<String> readAllLines(Charset charset) throws IOException;


    OutputStream newOutputStream() throws IOException;

    void writeAllBytes(byte[] bytes) throws IOException;

    void writeAllLines(Iterable<String> lines) throws IOException;


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


    @Override boolean equals(Object other);

    @Override int hashCode();

    @Override String toString();
}
