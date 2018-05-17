package mb.pie.vfs.path;

import mb.pie.vfs.access.DirAccess;
import mb.pie.vfs.list.PathMatcher;
import mb.pie.vfs.list.PathWalker;

import javax.annotation.Nullable;
import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public interface PPath extends Serializable {
    URI getUri();

    Path getJavaPath();


    boolean isDir();

    boolean isFile();

    boolean exists();

    long lastModifiedTimeMs() throws IOException;


    PPath normalized();

    PPath relativizeFrom(PPath other);

    String relativizeStringFrom(PPath other);

    @Nullable PPath parent();

    @Nullable PPath leaf();

    @Nullable String extension();


    PPath resolve(PPath other);

    PPath resolve(String other);

    PPath extend(String other);

    PPath replaceExtension(String extension);


    default Stream<PPath> list() throws IOException {
        return list(PPaths.allPathMatcher());
    }

    Stream<PPath> list(PathMatcher matcher) throws IOException;

    default Stream<PPath> walk() throws IOException {
        return walk(PPaths.allPathWalker(), null);
    }

    default Stream<PPath> walk(DirAccess access) throws IOException {
        return walk(PPaths.allPathWalker(), access);
    }

    default Stream<PPath> walk(PathWalker walker) throws IOException {
        return walk(walker, null);
    }

    Stream<PPath> walk(PathWalker walker, @Nullable DirAccess access) throws IOException;


    InputStream inputStream() throws IOException;

    byte[] readAllBytes() throws IOException;

    List<String> readAllLines(Charset cs) throws IOException;


    OutputStream outputStream() throws IOException;


    void touchFile() throws IOException;

    void touchDirectory() throws IOException;


    void createFile() throws IOException;

    void createDirectory() throws IOException;

    void createDirectories() throws IOException;

    void createParentDirectories() throws IOException;


    boolean deleteFile() throws IOException;


    String toString();
}
