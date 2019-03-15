package mb.pie.api.fs.stamp;

import mb.fs.api.node.FSNode;
import mb.fs.api.node.match.FSNodeMatcher;
import mb.fs.api.node.walk.FSNodeWalker;
import mb.pie.api.fs.FileSystemResource;
import mb.pie.api.stamp.ResourceStamper;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.stream.Stream;

interface ModifiedResourceStamperTrait extends ResourceStamper<FileSystemResource> {
    default long getUnknown() {
        return Long.MIN_VALUE;
    }

    default long modified(FSNode node, @Nullable FSNodeMatcher matcher) throws IOException {
        if(node.isDirectory()) {
            return modifiedDir(node, matcher);
        }
        if(node.isFile()) {
            return node.getLastModifiedTime().toEpochMilli();
        }
        return getUnknown();
    }

    default long modifiedRec(FSNode node, @Nullable FSNodeWalker walker, @Nullable FSNodeMatcher matcher) throws IOException {
        if(node.isDirectory()) {
            return modifiedDirRec(node, walker, matcher);
        }
        if(node.isFile()) {
            return node.getLastModifiedTime().toEpochMilli();
        }
        return getUnknown();
    }

    default long modifiedDir(FSNode dir, @Nullable FSNodeMatcher matcher) throws IOException {
        if(matcher == null) {
            return dir.getLastModifiedTime().toEpochMilli();
        }
        final long[] lastModified = {getUnknown()};
        try(final Stream<? extends FSNode> stream = dir.list(matcher)) {
            stream.forEach((node) -> {
                try {
                    final long modified = node.getLastModifiedTime().toEpochMilli();
                    lastModified[0] = Math.max(lastModified[0], modified);
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch(UncheckedIOException e) {
            throw e.getCause();
        }
        return lastModified[0];
    }

    default long modifiedDirRec(FSNode dir, @Nullable FSNodeWalker walker, @Nullable FSNodeMatcher matcher) throws IOException {
        final long[] lastModified = {getUnknown()};
        final boolean useWalker = walker != null && matcher != null;
        try(final Stream<? extends FSNode> stream = useWalker ? dir.walk(walker, matcher) : dir.walk()) {
            stream.forEach((node) -> {
                try {
                    final long modified = node.getLastModifiedTime().toEpochMilli();
                    lastModified[0] = Math.max(lastModified[0], modified);
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch(UncheckedIOException e) {
            throw e.getCause();
        }
        return lastModified[0];
    }
}
