package mb.pie.vfs.path;

import mb.pie.vfs.access.DirAccess;
import mb.pie.vfs.list.PathMatcher;
import mb.pie.vfs.list.*;

import javax.annotation.Nullable;
import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class PPathImpl implements PPath {
    private static final long serialVersionUID = 1L;

    private final URI uri;

    private transient @Nullable Path pathCache;


    public PPathImpl(URI uri, @Nullable Path path) {
        this.uri = uri;
        this.pathCache = path;
    }

    public PPathImpl(URI uri) {
        this(uri, null);
    }

    public PPathImpl(Path path) {
        this(path.toUri(), null);
    }


    @Override public URI getUri() {
        return uri;
    }

    @Override public Path getJavaPath() {
        if(pathCache == null) {
            pathCache = Paths.get(uri);
        }
        return pathCache;
    }


    @Override public boolean exists() {
        return Files.exists(getJavaPath());
    }

    @Override public boolean isFile() {
        return Files.isRegularFile(getJavaPath());
    }

    @Override public boolean isDir() {
        return Files.isDirectory(getJavaPath());
    }

    @Override public long lastModifiedTimeMs() throws IOException {
        return Files.getLastModifiedTime(getJavaPath()).toMillis();
    }


    @Override public PPath normalized() {
        final Path normalized = getJavaPath().normalize();
        return new PPathImpl(normalized);
    }

    @Override public PPath relativizeFrom(PPath other) {
        final Path path = getJavaPath();
        final Path relative = path.relativize(other.getJavaPath());
        return new PPathImpl(relative);
    }

    @Override public String relativizeStringFrom(PPath other) {
        final Path path = getJavaPath();
        final Path relative = path.relativize(other.getJavaPath());
        return relative.toString();
    }

    @Override public @Nullable PPath parent() {
        final Path parent = getJavaPath().getParent();
        if(parent == null) {
            return null;
        }
        return new PPathImpl(parent);
    }

    @Override public @Nullable PPath leaf() {
        final Path filePath = getJavaPath().getFileName();
        if(filePath == null) {
            return null;
        }
        return new PPathImpl(filePath);
    }

    @Override public @Nullable String extension() {
        final Path filePath = getJavaPath().getFileName();
        if(filePath == null) {
            return null;
        }

        final String fileName = getJavaPath().getFileName().toString();
        final int i = fileName.lastIndexOf('.');
        if(i > 0) {
            return fileName.substring(i + 1);
        }
        return null;
    }


    @Override public PPath resolve(PPath other) {
        final Path thisJavaPath = getJavaPath();
        final Path otherJavaPath = other.getJavaPath();
        final Path resolved = thisJavaPath.resolve(otherJavaPath);
        return new PPathImpl(resolved);
    }

    @Override public PPath resolve(String other) {
        final Path thisJavaPath = getJavaPath().normalize();
        final Path resolved = thisJavaPath.resolve(other);
        return new PPathImpl(resolved);
    }

    @Override public PPath extend(String other) {
        final Path path = getJavaPath();
        final Path filenamePath = path.getFileName();
        if(filenamePath == null) {
            return this;
        }
        final String filename = filenamePath.toString() + other;
        return new PPathImpl(path.resolveSibling(filename));
    }

    @Override public PPath replaceExtension(String extension) {
        final Path path = getJavaPath();
        final Path filenamePath = path.getFileName();
        if(filenamePath == null) {
            return this;
        }
        final String filename = filenamePath.toString();
        final int dotIndex = filename.lastIndexOf('.');
        final String filenameNoExt = dotIndex != -1 ? filename.substring(0, dotIndex) : filename;
        final String filenameNewExt = filenameNoExt + "." + extension;
        return new PPathImpl(path.resolveSibling(filenameNewExt));
    }


    @Override public Stream<PPath> list() throws IOException {
        // @formatter:off
        return Files
            .list(getJavaPath())
            .map(PPathImpl::new);
        // @formatter:on
    }

    @Override public Stream<PPath> list(PathMatcher matcher) throws IOException {
        // @formatter:off
        return Files
            .list(getJavaPath())
            .map(path -> (PPath) new PPathImpl(path))
            .filter((PPath p) -> matcher.matches(p, this));
        // @formatter:on
    }

    @Override public Stream<PPath> walk() throws IOException {
        // @formatter:off
        return Files
            .walk(getJavaPath())
            .map(PPathImpl::new);
        // @formatter:on
    }

    @Override public Stream<PPath> walk(PathWalker walker, @Nullable DirAccess access) throws IOException {
        final Stream.Builder<PPath> streamBuilder = Stream.builder();
        final PathWalkerVisitor visitor = new PathWalkerVisitor(walker, this, access, streamBuilder);
        Files.walkFileTree(getJavaPath(), visitor);
        return streamBuilder.build();
    }


    @Override public InputStream inputStream() throws IOException {
        return Files.newInputStream(getJavaPath(), StandardOpenOption.READ);
    }

    @Override public byte[] readAllBytes() throws IOException {
        return Files.readAllBytes(getJavaPath());
    }

    @Override public List<String> readAllLines(Charset cs) throws IOException {
        return Files.readAllLines(getJavaPath(), cs);
    }

    @Override public OutputStream outputStream() throws IOException {
        return Files.newOutputStream(getJavaPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING);
    }

    @Override public void touchFile() throws IOException {
        if(exists()) {
            Files.setLastModifiedTime(getJavaPath(), FileTime.from(Instant.now()));
        } else {
            createFile();
        }
    }

    @Override public void touchDirectory() throws IOException {
        if(exists()) {
            Files.setLastModifiedTime(getJavaPath(), FileTime.from(Instant.now()));
        } else {
            createDirectories();
        }
    }

    @Override public void createFile() throws IOException {
        Files.createFile(getJavaPath());
    }

    @Override public void createDirectory() throws IOException {
        Files.createDirectory(getJavaPath());
    }

    @Override public void createDirectories() throws IOException {
        Files.createDirectories(getJavaPath());
    }

    @Override public void createParentDirectories() throws IOException {
        final PPath parent = parent();
        if(parent != null) {
            parent.createDirectories();
        }
    }

    @Override public boolean deleteFile() throws IOException {
        return Files.deleteIfExists(getJavaPath());
    }

    @Override public boolean deleteAll() throws IOException {
        try {
            final Path javaPath = getJavaPath();
            if(!Files.exists(javaPath)) {
                return false;
            }
            return Files.walk(javaPath)
                .sorted(Comparator.reverseOrder())
                .map(path -> {
                    try {
                        return Files.deleteIfExists(path);
                    } catch(IOException e) {
                        throw new UncheckedIOException(e);
                    }
                })
                .allMatch(b -> b);
        } catch(UncheckedIOException e) {
            throw e.getCause();
        }
    }


    @Override public String toString() {
        return getJavaPath().toString();
    }

    @Override public int hashCode() {
        return uri.hashCode();
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final PPathImpl other = (PPathImpl) obj;
        if(!uri.equals(other.uri))
            return false;
        return true;
    }
}
