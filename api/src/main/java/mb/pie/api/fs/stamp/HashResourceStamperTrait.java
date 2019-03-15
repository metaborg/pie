package mb.pie.api.fs.stamp;

import mb.fs.api.node.FSNode;
import mb.fs.api.node.match.FSNodeMatcher;
import mb.fs.api.node.walk.FSNodeWalker;
import mb.pie.api.fs.FileSystemResource;
import mb.pie.api.stamp.ResourceStamper;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Stream;

interface HashResourceStamperTrait extends ResourceStamper<FileSystemResource> {
    default MessageDigest createDigester() {
        try {
            return MessageDigest.getInstance("SHA-1");
        } catch(NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    default void update(MessageDigest digest, FSNode node, @Nullable FSNodeMatcher matcher) throws IOException {
        if(node.isDirectory()) {
            updateDir(digest, node, matcher);
        }
        if(node.isFile()) {
            updateFile(digest, node);
        }
    }

    default void updateRec(MessageDigest digest, FSNode node, @Nullable FSNodeWalker walker, @Nullable FSNodeMatcher matcher) throws IOException {
        if(node.isDirectory()) {
            updateDirRec(digest, node, walker, matcher);
        }
        if(node.isFile()) {
            updateFile(digest, node);
        }
    }

    default void updateDir(MessageDigest digest, FSNode dir, @Nullable FSNodeMatcher matcher) throws IOException {
        final boolean useMatcher = matcher != null;
        try(final Stream<? extends FSNode> stream = useMatcher ? dir.list(matcher) : dir.list()) {
            stream.forEach((node) -> {
                try {
                    if(node.isFile()) {
                        updateFile(digest, node);
                    }
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch(UncheckedIOException e) {
            throw e.getCause();
        }
    }

    default void updateDirRec(MessageDigest digest, FSNode dir, @Nullable FSNodeWalker walker, @Nullable FSNodeMatcher matcher) throws IOException {
        final boolean useWalker = walker != null && matcher != null;
        try(final Stream<? extends FSNode> stream = useWalker ? dir.walk(walker, matcher) : dir.walk()) {
            stream.forEach((node) -> {
                try {
                    if(node.isFile()) {
                        updateFile(digest, node);
                    }
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch(UncheckedIOException e) {
            throw e.getCause();
        }
    }

    default void updateFile(MessageDigest digest, FSNode file) throws IOException {
        digest.update(file.readAllBytes());
    }
}
