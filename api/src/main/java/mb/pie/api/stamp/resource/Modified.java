package mb.pie.api.stamp.resource;

import mb.resource.ReadableResource;
import mb.resource.fs.FSResource;
import mb.resource.fs.match.ResourceMatcher;
import mb.resource.fs.walk.ResourceWalker;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.stream.Stream;

class Modified {
    static long modified(FSResource resource, @Nullable ResourceMatcher matcher) throws IOException {
        if(resource.isFile()) {
            return modifiedFile(resource);
        }
        if(resource.isDirectory()) {
            return modifiedDir(resource, matcher);
        }
        return getUnknown();
    }

    static long modifiedRec(FSResource resource, @Nullable ResourceWalker walker, @Nullable ResourceMatcher matcher) throws IOException {
        if(resource.isFile()) {
            return modifiedFile(resource);
        }
        if(resource.isDirectory()) {
            return modifiedDirRec(resource, walker, matcher);
        }
        return getUnknown();
    }

    static long modifiedFile(ReadableResource resource) throws IOException {
        return resource.getLastModifiedTime().toEpochMilli();
    }

    private static long modifiedDir(FSResource dir, @Nullable ResourceMatcher matcher) throws IOException {
        if(matcher == null) {
            return modifiedFile(dir);
        }
        final long[] lastModified = {getUnknown()}; // Use array to allow access to non-final variable in closure.
        try(final Stream<FSResource> stream = dir.list(matcher)) {
            stream.forEach((resource) -> {
                try {
                    final long modified = modifiedFile(resource);
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

    private static long modifiedDirRec(FSResource dir, @Nullable ResourceWalker walker, @Nullable ResourceMatcher matcher) throws IOException {
        final long[] lastModified = {getUnknown()}; // Use array to allow access to non-final variable in closure.
        final boolean useWalker = walker != null && matcher != null;
        try(final Stream<FSResource> stream = useWalker ? dir.walk(walker, matcher) : dir.walk()) {
            stream.forEach((resource) -> {
                try {
                    final long modified = modifiedFile(resource);
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

    private static long getUnknown() {
        return Long.MIN_VALUE;
    }
}
