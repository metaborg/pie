package mb.fs.java;

import mb.fs.api.path.*;

import javax.annotation.Nullable;
import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;

public class JavaFSPath implements Serializable {
    private static final long serialVersionUID = 1L;

    private final URI uri; // URI version of the path which can be serialized and deserialized.
    transient Path javaPath; // Transient and non-final for deserialization in readObject. Invariant: always nonnull.


    public JavaFSPath(Path javaPath) {
        this.uri = javaPath.toUri();
        this.javaPath = javaPath;
    }

    public JavaFSPath(URI uri) {
        this.uri = uri;
        this.javaPath = createJavaPath(uri);
    }

    public JavaFSPath(FSPath path) {
        this(createJavaPath(path));
    }

    public JavaFSPath(File javaFile) {
        this(createJavaPath(javaFile));
    }

    public JavaFSPath(String localPathStr) {
        this(createJavaPath(localPathStr));
    }


    public Path getJavaPath() {
        return javaPath;
    }

    public URI getURI() {
        return uri;
    }


    public FSPath getGeneralPath() {
        return createGeneralPath(javaPath);
    }


    public @Nullable JavaFSPath getParent() {
        final @Nullable Path parentJavaPath = this.javaPath.getParent();
        if(parentJavaPath == null) {
            return null;
        }
        return new JavaFSPath(parentJavaPath);
    }

    public JavaFSPath getNormalized() {
        final Path normalizedJavaPath = this.javaPath.normalize();
        return new JavaFSPath(normalizedJavaPath);
    }

    public @Nullable JavaFSPath getRoot() {
        final @Nullable Path rootJavaPath = this.javaPath.getRoot();
        if(rootJavaPath == null) {
            return null;
        }
        return new JavaFSPath(rootJavaPath);
    }


    public JavaFSPath appendSegment(String segment) {
        final Path javaPath = this.javaPath.resolve(segment);
        return new JavaFSPath(javaPath);
    }

    public JavaFSPath appendSegments(Collection<String> segments) {
        final Path relJavaPath = createJavaPath(segments);
        final Path javaPath = this.javaPath.resolve(relJavaPath);
        return new JavaFSPath(javaPath);
    }

    public JavaFSPath appendSegments(Iterable<String> segments) {
        final ArrayList<String> segmentsList = new ArrayList<>();
        segments.forEach(segmentsList::add);
        return appendSegments(segmentsList);
    }

    public JavaFSPath appendSegments(String... segments) {
        final Path relJavaPath = createJavaPath(segments);
        final Path javaPath = this.javaPath.resolve(relJavaPath);
        return new JavaFSPath(javaPath);
    }

    public JavaFSPath appendRelativePath(RelativeFSPath relativePath) {
        final Path relJavaPath = createJavaPath(relativePath.getSegments());
        final Path javaPath = this.javaPath.resolve(relJavaPath);
        return new JavaFSPath(javaPath);
    }


    public JavaFSPath appendToLeafSegment(String str) {
        final String fileName = this.javaPath.getFileName().toString();
        final String newFileName = fileName + str;
        final Path javaPath = this.javaPath.resolveSibling(newFileName);
        return new JavaFSPath(javaPath);
    }

    public JavaFSPath replaceLeafSegment(String segment) {
        final Path javaPath = this.javaPath.resolveSibling(segment);
        return new JavaFSPath(javaPath);
    }

    public JavaFSPath applyToLeafSegment(Function<String, String> func) {
        final String fileName = this.javaPath.getFileName().toString();
        final Path javaPath = this.javaPath.resolveSibling(func.apply(fileName));
        return new JavaFSPath(javaPath);
    }


    private static Path createJavaPath(FSPath path) {
        if(!path.getSelectorRoot().equals(JavaFileSystem.rootSelector)) {
            throw new InvalidFSPathRuntimeException(
                "Failed to create local filesystem path; general path '" + path + "' does not have '" + JavaFileSystem.rootSelector + "' as root selector");
        }
        return createJavaPath(path.getSegments());
    }

    private static Path createJavaPath(Collection<String> segments) {
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

    private static Path createJavaPath(String... segments) {
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

    private static Path createJavaPath(File javaFile) {
        try {
            return FileSystems.getDefault().getPath(javaFile.getPath());
        } catch(InvalidPathException e) {
            throw new InvalidFSPathRuntimeException(e);
        }
    }

    private static Path createJavaPath(URI uri) {
        try {
            return Paths.get(uri);
        } catch(IllegalArgumentException | FileSystemNotFoundException e) {
            throw new InvalidFSPathRuntimeException(e);
        }
    }

    private static Path createJavaPath(String localPathStr) {
        try {
            return FileSystems.getDefault().getPath(localPathStr);
        } catch(InvalidPathException e) {
            throw new InvalidFSPathRuntimeException(e);
        }
    }


    private static FSPath createGeneralPath(Path javaPath) {
        // TODO: handle Windows device in javaPath segments.
        final ArrayList<String> segments = new ArrayList<>(javaPath.getNameCount());
        for(Path pathSegment : javaPath) {
            final String segment = pathSegment.toString();
            segments.add(segment);
        }
        return FSPath.from(JavaFileSystem.rootSelector, segments);
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


    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        in.defaultReadObject();
        this.javaPath = createJavaPath(this.uri);
    }
}
