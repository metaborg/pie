package mb.pie.vfs.list;

import mb.pie.vfs.access.DirAccess;
import mb.pie.vfs.path.PPath;
import mb.pie.vfs.path.PPathImpl;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;

public class PathWalkerVisitor implements FileVisitor<Path> {
    private final PathWalker walker;
    private final PPath root;
    private final @Nullable DirAccess access;
    private final Stream.Builder<PPath> streamBuilder;


    public PathWalkerVisitor(PathWalker walker, PPath root, @Nullable DirAccess access,
        Stream.Builder<PPath> streamBuilder) {
        this.walker = walker;
        this.root = root;
        this.access = access;
        this.streamBuilder = streamBuilder;
    }


    @Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        final PPath path = new PPathImpl(dir);
        if(walker.matches(path, root)) {
            streamBuilder.add(path);
        }
        if(walker.traverse(path, root)) {
            if(access != null) {
                access.readDir(path);
            }
            return FileVisitResult.CONTINUE;
        }
        return FileVisitResult.SKIP_SUBTREE;
    }

    @Override public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
        final PPath path = new PPathImpl(file);
        if(walker.matches(path, root)) {
            streamBuilder.add(path);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override public FileVisitResult visitFileFailed(Path file, IOException exc) {
        return FileVisitResult.CONTINUE;
    }

    @Override public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        return FileVisitResult.CONTINUE;
    }
}
