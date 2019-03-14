package mb.fs.java;

import mb.fs.api.node.FSNode;
import mb.fs.api.node.FSNodeAccess;
import mb.fs.api.node.FSNodeType;
import mb.fs.api.node.match.FSNodeMatcher;
import mb.fs.api.node.walk.FSNodeWalker;
import mb.fs.api.path.FSPath;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class JavaFSNode implements FSNode, Serializable {
    final JavaFSPath path;


    public JavaFSNode(JavaFSPath path) {
        this.path = path;
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


    public Path getJavaPath() {
        return path.javaPath;
    }

    public URI getURI() {
        return path.uri;
    }

    public boolean isLocalPath() {
        return path.javaPath.getFileSystem().equals(FileSystems.getDefault());
    }


    @Override public JavaFSPath getPath() {
        return path;
    }

    @Override public JavaFileSystem getFileSystem() {
        return JavaFileSystem.instance;
    }

    @Override public String getFileSystemId() {
        return JavaFileSystem.id;
    }


    @Override public @Nullable JavaFSNode getParent() {
        final @Nullable JavaFSPath newPath = path.getParent();
        if(newPath == null) {
            return null;
        }
        return new JavaFSNode(newPath);
    }

    @Override public @Nullable JavaFSNode getRoot() {
        final @Nullable JavaFSPath newPath = path.getRoot();
        if(newPath == null) {
            return null;
        }
        return new JavaFSNode(newPath);
    }

    @Override public @Nullable String getLeaf() {
        return path.getLeaf();
    }

    @Override public JavaFSNode appendSegment(String segment) {
        final JavaFSPath newPath = path.appendSegment(segment);
        return new JavaFSNode(newPath);
    }

    @Override public JavaFSNode appendSegments(Iterable<String> segments) {
        final JavaFSPath newPath = path.appendSegments(segments);
        return new JavaFSNode(newPath);
    }

    @Override public JavaFSNode appendSegments(Collection<String> segments) {
        final JavaFSPath newPath = path.appendSegments(segments);
        return new JavaFSNode(newPath);
    }

    @Override public JavaFSNode appendSegments(List<String> segments) {
        final JavaFSPath newPath = path.appendSegments(segments);
        return new JavaFSNode(newPath);
    }

    @Override public JavaFSNode appendSegments(String... segments) {
        final JavaFSPath newPath = path.appendSegments(segments);
        return new JavaFSNode(newPath);
    }

    @Override public JavaFSNode appendRelativePath(FSPath relativePath) {
        final JavaFSPath newPath = path.appendRelativePath(relativePath);
        return new JavaFSNode(newPath);
    }


    @Override public JavaFSNode replaceLeaf(String str) {
        final JavaFSPath newPath = path.replaceLeaf(str);
        return new JavaFSNode(newPath);
    }

    @Override public JavaFSNode appendToLeaf(String str) {
        final JavaFSPath newPath = path.appendToLeaf(str);
        return new JavaFSNode(newPath);
    }

    @Override public JavaFSNode applyToLeaf(Function<String, String> func) {
        final JavaFSPath newPath = path.applyToLeaf(func);
        return new JavaFSNode(newPath);
    }

    @Override public JavaFSNode replaceLeafExtension(String extension) {
        final JavaFSPath newPath = path.replaceLeafExtension(extension);
        return new JavaFSNode(newPath);
    }

    @Override public JavaFSNode appendExtensionToLeaf(String extension) {
        final JavaFSPath newPath = path.appendExtensionToLeaf(extension);
        return new JavaFSNode(newPath);
    }

    @Override public JavaFSNode applyToLeafExtension(Function<String, String> func) {
        final JavaFSPath newPath = path.applyToLeafExtension(func);
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


    @Override public Stream<JavaFSNode> list() throws IOException {
        return Files.list(path.javaPath).map(JavaFSNode::new);
    }

    @Override public Stream<JavaFSNode> list(FSNodeMatcher matcher) throws IOException {
        try {
            return Files.list(path.javaPath).map(JavaFSNode::new).filter((n) -> {
                try {
                    return matcher.matches(n, this);
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch(UncheckedIOException e) {
            throw e.getCause();
        }
    }

    @Override public Stream<JavaFSNode> walk() throws IOException {
        return Files.walk(path.javaPath).map(JavaFSNode::new);
    }

    @Override public Stream<JavaFSNode> walk(FSNodeWalker walker, FSNodeMatcher matcher) throws IOException {
        return walk(walker, matcher, null);
    }

    @Override
    public Stream<JavaFSNode> walk(FSNodeWalker walker, FSNodeMatcher matcher, @Nullable FSNodeAccess access) throws IOException {
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

    @Override public List<String> readAllLines(Charset charset) throws IOException {
        return Files.readAllLines(path.javaPath, charset);
    }


    @Override public OutputStream newOutputStream() throws IOException {
        return Files.newOutputStream(path.javaPath, StandardOpenOption.WRITE, StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING);
    }

    @Override public void writeAllBytes(byte[] bytes) throws IOException {
        Files.write(path.javaPath, bytes);
    }

    @Override public void writeAllLines(Iterable<String> lines) throws IOException {
        Files.write(path.javaPath, lines);
    }


    @Override public void copyTo(FSNode other) throws IOException {
        if(!(other instanceof JavaFSNode)) {
            throw new RuntimeException(
                "Cannot copy from " + this + " to target " + other + ", target is not a Java file system node");
        }
        Files.copy(path.javaPath, ((JavaFSNode) other).path.javaPath);
    }

    @Override public void moveTo(FSNode other) throws IOException {
        if(!(other instanceof JavaFSNode)) {
            throw new RuntimeException(
                "Cannot move from " + this + " to target " + other + ", target is not a Java file system node");
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
            Files.createDirectories(path.javaPath);
        }
        if(!exists()) {
            Files.createDirectory(path.javaPath);
        }
    }

    @Override public void createParents() throws IOException {
        final @Nullable JavaFSNode parent = getParent();
        if(parent != null) {
            Files.createDirectories(parent.path.javaPath);
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
