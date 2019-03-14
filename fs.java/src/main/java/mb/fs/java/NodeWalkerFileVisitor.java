package mb.fs.java;

import mb.fs.api.node.FSNodeAccess;
import mb.fs.api.node.match.FSNodeMatcher;
import mb.fs.api.node.walk.FSNodeWalker;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

public class NodeWalkerFileVisitor implements FileVisitor<Path> {
    private final FSNodeMatcher matcher;
    private final FSNodeWalker walker;
    private final JavaFSNode root;
    private final Stream.Builder<JavaFSNode> streamBuilder;
    private final @Nullable FSNodeAccess access;


    public NodeWalkerFileVisitor(FSNodeWalker walker, FSNodeMatcher matcher, JavaFSNode root, Builder<JavaFSNode> streamBuilder,
                                 @Nullable FSNodeAccess access) {
        this.matcher = matcher;
        this.walker = walker;
        this.root = root;
        this.streamBuilder = streamBuilder;
        this.access = access;
    }


    @Override
    public FileVisitResult preVisitDirectory(@NonNull Path dir, @NonNull BasicFileAttributes attrs) throws IOException {
        final JavaFSNode node = new JavaFSNode(dir);
        if(access != null) {
            access.read(node);
        }
        if(matcher.matches(node, root)) {
            streamBuilder.add(node);
        }
        if(walker.traverse(node, root)) {
            return FileVisitResult.CONTINUE;
        }
        return FileVisitResult.SKIP_SUBTREE;
    }

    @Override
    public FileVisitResult visitFile(@NonNull Path file, @NonNull BasicFileAttributes attrs) throws IOException {
        final JavaFSNode node = new JavaFSNode(file);
        if(access != null) {
            access.read(node);
        }
        if(matcher.matches(node, root)) {
            streamBuilder.add(node);
        }
        return FileVisitResult.CONTINUE;
    }

    @Override public FileVisitResult visitFileFailed(@NonNull Path file, @NonNull IOException exc) {
        // TODO: handle visit file failed.
        return FileVisitResult.CONTINUE;
    }

    @Override public FileVisitResult postVisitDirectory(@NonNull Path dir, @Nullable IOException exc) {
        // TODO: handle visit directory failed (exc != null).
        return FileVisitResult.CONTINUE;
    }
}
