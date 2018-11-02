package mb.fs.java;

import mb.fs.api.node.*;
import mb.fs.api.path.FSPath;
import mb.fs.api.path.RelativeFSPath;

import javax.annotation.Nullable;
import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class JavaFSNode implements FSNode, Serializable {
    private static final long serialVersionUID = 1L;

    private final JavaFSPath path;


    public JavaFSNode(JavaFSPath path) {
        this.path = path;
    }

    public JavaFSNode(FSPath path) {
        this.path = new JavaFSPath(path);
    }

    public JavaFSNode(Path javaPath) {
        this.path = new JavaFSPath(javaPath);
    }

    public JavaFSNode(URI uri) {
        this.path = new JavaFSPath(uri);
    }

    public JavaFSNode(File file) {
        this.path = new JavaFSPath(file);
    }

    public JavaFSNode(String localPathStr) {
        this.path = new JavaFSPath(localPathStr);
    }


    public JavaFSPath getJavaPath() {
        return path;
    }

    @Override public FSPath getPath() {
        return path.getGeneralPath();
    }


    @Override public @Nullable JavaFSNode getParent() {
        final @Nullable JavaFSPath newPath = path.getParent();
        if(newPath == null) {
            return null;
        }
        return new JavaFSNode(newPath);
    }

    @Override public FSNode appendSegment(String segment) {
        final JavaFSPath newPath = path.appendSegment(segment);
        return new JavaFSNode(newPath);
    }

    @Override public FSNode appendSegments(List<String> segments) {
        final JavaFSPath newPath = path.appendSegments(segments);
        return new JavaFSNode(newPath);
    }

    @Override public FSNode appendSegments(Collection<String> segments) {
        final JavaFSPath newPath = path.appendSegments(segments);
        return new JavaFSNode(newPath);
    }

    @Override public FSNode appendSegments(Iterable<String> segments) {
        final JavaFSPath newPath = path.appendSegments(segments);
        return new JavaFSNode(newPath);
    }

    @Override public FSNode appendSegments(String... segments) {
        final JavaFSPath newPath = path.appendSegments(segments);
        return new JavaFSNode(newPath);
    }

    @Override public FSNode appendRelativePath(RelativeFSPath relativePath) {
        final JavaFSPath newPath = path.appendRelativePath(relativePath);
        return new JavaFSNode(newPath);
    }

    @Override public JavaFSNode appendToLeafSegment(String str) {
        final JavaFSPath newPath = path.appendToLeafSegment(str);
        return new JavaFSNode(newPath);
    }

    @Override public JavaFSNode replaceLeafSegment(String str) {
        final JavaFSPath newPath = path.replaceLeafSegment(str);
        return new JavaFSNode(newPath);
    }

    @Override public FSNode applyToLeafSegment(Function<String, String> func) {
        final JavaFSPath newPath = path.applyToLeafSegment(func);
        return new JavaFSNode(newPath);
    }


    @Override public FSNodeType getType() {
        if(!Files.exists(path.javaPath)) {
            return FSNodeType.NonExistent;
        } else if(Files.isDirectory(path.javaPath)) {
            return FSNodeType.Directory;
        } else {
            return FSNodeType.File;
        }
    }

    @Override public boolean isFile() {
        return Files.isRegularFile(path.javaPath);
    }

    @Override public boolean isDirectory() {
        return Files.isDirectory(path.javaPath);
    }

    @Override public boolean exists() {
        return Files.exists(path.javaPath);
    }

    @Override public boolean isReadable() {
        return Files.isReadable(path.javaPath);
    }

    @Override public boolean isWritable() {
        return Files.isWritable(path.javaPath);
    }

    @Override public Instant getLastModifiedTime() throws IOException {
        return Files.getLastModifiedTime(path.javaPath).toInstant();
    }

    @Override public void setLastModifiedTime(Instant time) throws IOException {
        Files.setLastModifiedTime(path.javaPath, FileTime.from(time));
    }

    @Override public long getSize() throws IOException {
        return Files.size(path.javaPath);
    }


    @Override public Stream<? extends FSNode> list() throws IOException {
        return Files.list(path.javaPath).map(JavaFSNode::new);
    }

    @Override public Stream<? extends FSNode> list(FSNodeMatcher matcher) throws IOException {
        return Files.list(path.javaPath).map(JavaFSNode::new).filter((n) -> matcher.matches(n, this));
    }

    @Override public Stream<? extends FSNode> walk() throws IOException {
        return Files.walk(path.javaPath).map(JavaFSNode::new);
    }

    @Override
    public Stream<? extends FSNode> walk(FSNodeWalker walker, FSNodeMatcher matcher, @Nullable FSNodeAccess access) throws IOException {
        final Stream.Builder<JavaFSNode> streamBuilder = Stream.builder();
        final NodeWalkerFileVisitor visitor = new NodeWalkerFileVisitor(walker, matcher, this, streamBuilder, access);
        Files.walkFileTree(path.javaPath, visitor);
        return streamBuilder.build();
    }


    @Override public InputStream newInputStream() throws IOException {
        return Files.newInputStream(path.javaPath, StandardOpenOption.READ);
    }

    @Override public byte[] readAllBytes() throws IOException {
        return Files.readAllBytes(path.javaPath);
    }


    @Override public OutputStream newOutputStream() throws IOException {
        return Files.newOutputStream(path.javaPath, StandardOpenOption.WRITE, StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING);
    }


    @Override public void copyTo(FSNode other) throws IOException {
        if(!(other instanceof JavaFSNode)) {
            throw new RuntimeException("Cannot copy from " + this + " to target " + other + ", target is not a Java file system node");
        }
        Files.copy(path.javaPath, ((JavaFSNode) other).path.javaPath);
    }

    @Override public void moveTo(FSNode other) throws IOException {
        if(!(other instanceof JavaFSNode)) {
            throw new RuntimeException("Cannot move from " + this + " to target " + other + ", target is not a Java file system node");
        }
        Files.move(path.javaPath, ((JavaFSNode) other).path.javaPath);
    }


    @Override public void createFile(boolean createParents) throws IOException {
        if(createParents) {
            createParents();
        }
        Files.createFile(path.javaPath);
    }

    @Override public void createDirectory(boolean createParents) throws IOException {
        if(createParents) {
            createParents();
        }
        Files.createDirectory(path.javaPath);
    }

    @Override public void createParents() throws IOException {
        // OPTO: non-recursive implementation.
        final @Nullable JavaFSNode parent = getParent();
        if(parent != null) {
            parent.createDirectory(true);
        }
    }


    @Override public void delete(boolean deleteContents) throws IOException {
        if(deleteContents) {
            try {
                if(!Files.exists(path.javaPath)) {
                    return;
                }
                Files.walk(path.javaPath)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch(IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
            } catch(UncheckedIOException e) {
                throw e.getCause();
            }
        } else {
            Files.deleteIfExists(path.javaPath);
        }
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final JavaFSNode that = (JavaFSNode) o;
        return path.equals(that.path);
    }

    @Override public int hashCode() {
        return path.hashCode();
    }

    @Override public String toString() {
        return path.toString();
    }
}
