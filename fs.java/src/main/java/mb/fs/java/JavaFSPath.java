package mb.fs.java;

import mb.fs.api.path.*;

import javax.annotation.Nullable;
import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;

/**
 * {@link FSPath} implementation for {@link java.nio.file.Path}s.
 */
public class JavaFSPath implements FSPath {
    private static final long serialVersionUID = 1L;

    final URI uri; // URI version of the path which can be serialized and deserialized.
    transient Path javaPath; // Transient and non-final for deserialization in readObject. Invariant: always nonnull.


    public JavaFSPath(Path javaPath) {
        this.uri = javaPath.toUri();
        this.javaPath = javaPath;
    }

    public JavaFSPath(URI uri) {
        this.uri = uri;
        this.javaPath = createJavaPath(uri);
    }

    public JavaFSPath(File javaFile) {
        this(createLocalPath(javaFile));
    }

    public JavaFSPath(String localPathStr) {
        this(createLocalPath(localPathStr));
    }


    /**
     * @return absolute path of the current working directory (given by {@code System.getProperty("user.dir")}.
     */
    public static JavaFSPath workingDirectory() {
        return new JavaFSPath(System.getProperty("user.dir"));
    }

    /**
     * @return absolute path of the current user's home directory (given by {@code System.getProperty("user.home")}.
     */
    public static JavaFSPath homeDirectory() {
        return new JavaFSPath(System.getProperty("user.home"));
    }


    /**
     * @return {@link Path} corresponding to this path.
     */
    public Path getJavaPath() {
        return javaPath;
    }

    /**
     * @return {@link URI} corresponding to this path.
     */
    public URI getURI() {
        return uri;
    }

    /**
     * @return true if this path is a local file system path, false otherwise.
     */
    public boolean isLocalPath() {
        return javaPath.getFileSystem().equals(FileSystems.getDefault());
    }

    /**
     * @return {@link JavaFSNode} for this path.
     */
    public JavaFSNode toNode() {
        return new JavaFSNode(this);
    }


    @Override public String getFileSystemId() {
        return JavaFileSystem.id;
    }


    @Override public boolean isAbsolute() {
        return javaPath.isAbsolute();
    }

    /**
     * @return this path if it {@link #isAbsolute()}, or returns an absolute path by appending this path to {@link #workingDirectory()}.
     */
    public JavaFSPath toAbsoluteFromWorkingDirectory() {
        if(javaPath.isAbsolute()) {
           return this;
        } else {
            return workingDirectory().appendRelativePath(this);
        }
    }

    /**
     * @return this path if it {@link #isAbsolute()}, or returns an absolute path by appending this path to {@link #homeDirectory()}.
     */
    public JavaFSPath toAbsoluteFromHomeDirectory() {
        if(javaPath.isAbsolute()) {
            return this;
        } else {
            return homeDirectory().appendRelativePath(this);
        }
    }


    @Override public int getSegmentCount() {
        return javaPath.getNameCount();
    }

    @Override public Iterable<String> getSegments() {
        return () -> new PathIterator(javaPath.iterator());
    }


    @Override public @Nullable JavaFSPath getParent() {
        final @Nullable Path parentJavaPath = this.javaPath.getParent();
        if(parentJavaPath == null) {
            return null;
        }
        return new JavaFSPath(parentJavaPath);
    }

    @Override public @Nullable JavaFSPath getRoot() {
        final @Nullable Path rootJavaPath = this.javaPath.getRoot();
        if(rootJavaPath == null) {
            return null;
        }
        return new JavaFSPath(rootJavaPath);
    }

    @Override public String getLeaf() {
        final @Nullable Path fileName = this.javaPath.getFileName();
        if(fileName == null) {
            return null;
        }
        return fileName.toString();
    }

    @Override public JavaFSPath getNormalized() {
        final Path normalizedJavaPath = this.javaPath.normalize();
        return new JavaFSPath(normalizedJavaPath);
    }

    @Override public JavaFSPath relativize(FSPath other) {
        if(!(other instanceof JavaFSPath)) {
            throw new InvalidFSPathRuntimeException(
                "Cannot relativize '" + this + "' relative to '" + other + "', it is not a Java file system path");
        }
        return relativize((JavaFSPath) other);
    }

    public JavaFSPath relativize(JavaFSPath other) {
        final Path javaRelativePath = javaPath.relativize(other.javaPath);
        return new JavaFSPath(javaRelativePath);
    }


    @Override public JavaFSPath appendSegment(String segment) {
        final Path javaPath = this.javaPath.resolve(segment);
        return new JavaFSPath(javaPath);
    }

    @Override public JavaFSPath appendSegments(Iterable<String> segments) {
        final ArrayList<String> segmentsList = new ArrayList<>();
        segments.forEach(segmentsList::add);
        return appendSegments(segmentsList);
    }

    @Override public JavaFSPath appendSegments(Collection<String> segments) {
        final Path relJavaPath = createLocalPath(segments);
        final Path javaPath = this.javaPath.resolve(relJavaPath);
        return new JavaFSPath(javaPath);
    }

    @Override public JavaFSPath appendSegments(List<String> segments) {
        final Path relJavaPath = createLocalPath(segments);
        final Path javaPath = this.javaPath.resolve(relJavaPath);
        return new JavaFSPath(javaPath);
    }

    @Override public JavaFSPath appendSegments(String... segments) {
        final Path relJavaPath = createLocalPath(segments);
        final Path javaPath = this.javaPath.resolve(relJavaPath);
        return new JavaFSPath(javaPath);
    }

    @Override public JavaFSPath appendRelativePath(FSPath relativePath) {
        if(!(relativePath instanceof JavaFSPath)) {
            throw new InvalidFSPathRuntimeException("Cannot append relative path " + relativePath + ", it is not a Java file system path");
        }
        return appendRelativePath((JavaFSPath) relativePath);
    }

    public JavaFSPath appendRelativePath(JavaFSPath relativePath) {
        if(relativePath.isAbsolute()) {
            throw new InvalidFSPathRuntimeException("Cannot append relative path " + relativePath + ", it is not a relative path");
        }
        final Path javaPath = this.javaPath.resolve(relativePath.javaPath);
        return new JavaFSPath(javaPath);
    }

    public JavaFSPath appendJavaPath(Path segments) {
        final Path javaPath = this.javaPath.resolve(segments);
        return new JavaFSPath(javaPath);
    }


    @Override public JavaFSPath appendToLeaf(String str) {
        final String fileName = this.javaPath.getFileName().toString();
        final String newFileName = fileName + str;
        final Path javaPath = this.javaPath.resolveSibling(newFileName);
        return new JavaFSPath(javaPath);
    }

    @Override public JavaFSPath replaceLeaf(String segment) {
        final Path javaPath = this.javaPath.resolveSibling(segment);
        return new JavaFSPath(javaPath);
    }

    @Override public JavaFSPath applyToLeaf(Function<String, String> func) {
        final String fileName = this.javaPath.getFileName().toString();
        final Path javaPath = this.javaPath.resolveSibling(func.apply(fileName));
        return new JavaFSPath(javaPath);
    }

    @Override public JavaFSPath replaceLeafExtension(String extension) {
        final @Nullable String leaf = getLeaf();
        if(leaf == null) {
            return this;
        }
        return replaceLeaf(FilenameExtensionUtil.replaceExtension(leaf, extension));
    }

    @Override public JavaFSPath appendExtensionToLeaf(String extension) {
        final @Nullable String leaf = getLeaf();
        if(leaf == null) {
            return this;
        }
        return replaceLeaf(FilenameExtensionUtil.appendExtension(leaf, extension));
    }

    @Override public JavaFSPath applyToLeafExtension(Function<String, String> func) {
        final @Nullable String leaf = getLeaf();
        if(leaf == null) {
            return this;
        }
        return replaceLeaf(FilenameExtensionUtil.applyToExtension(leaf, func));
    }


    private static Path createJavaPath(URI uri) {
        try {
            return Paths.get(uri);
        } catch(IllegalArgumentException | FileSystemNotFoundException e) {
            throw new InvalidFSPathRuntimeException(e);
        }
    }

    private static Path createLocalPath(Collection<String> segments) {
        final int segmentsSize = segments.size();
        if(segmentsSize == 0) {
            return FileSystems.getDefault().getPath("/");
        } else {
            @Nullable String first = null;
            final String[] more = new String[segmentsSize - 1];
            int i = 0;
            for(String segment : segments) {
                if(first == null) {
                    first = segment;
                } else {
                    more[++i] = segment;
                }
            }
            return FileSystems.getDefault().getPath(first, more);
        }
    }

    private static Path createLocalPath(String... segments) {
        final int segmentsSize = segments.length;
        if(segmentsSize == 0) {
            return FileSystems.getDefault().getPath("/");
        } else {
            final String first = segments[0];
            final String[] more = new String[segmentsSize - 1];
            System.arraycopy(segments, 1, more, 0, segmentsSize - 1);
            return FileSystems.getDefault().getPath(first, more);
        }
    }

    private static Path createLocalPath(File javaFile) {
        try {
            return FileSystems.getDefault().getPath(javaFile.getPath());
        } catch(InvalidPathException e) {
            throw new InvalidFSPathRuntimeException(e);
        }
    }

    private static Path createLocalPath(String localPathStr) {
        try {
            return FileSystems.getDefault().getPath(localPathStr);
        } catch(InvalidPathException e) {
            throw new InvalidFSPathRuntimeException(e);
        }
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final JavaFSPath javaPath = (JavaFSPath) o;
        return uri.equals(javaPath.uri);
    }

    @Override public int hashCode() {
        return uri.hashCode();
    }

    @Override public String toString() {
        return javaPath.toString();
    }


    @Override public int compareTo(FSPath other) {
        if(!(other instanceof JavaFSPath)) {
            throw new InvalidFSPathRuntimeException("Cannot compare to path " + other + ", it is not a Java file system path");
        }
        return compareTo((JavaFSPath) other);
    }

    public int compareTo(JavaFSPath other) {
        return this.javaPath.compareTo(other.javaPath);
    }


    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        in.defaultReadObject();
        this.javaPath = createJavaPath(this.uri);
    }
}
