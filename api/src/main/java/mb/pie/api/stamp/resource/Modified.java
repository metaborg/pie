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
    static long modified(ReadableResource resource) throws IOException {
        if(!resource.exists()) {
            return getMaximum();
        } else {
            return modifiedResource(resource);
        }
    }

    static long modified(FSResource resource, @Nullable ResourceMatcher matcher) throws IOException {
        if(!resource.exists()) {
            return getMaximum();
        } else if(resource.isFile()) {
            return modifiedResource(resource);
        } else if(resource.isDirectory()) {
            return modifiedDir(resource, matcher);
        }
        return getMinimum();
    }

    static long modifiedRec(FSResource resource, @Nullable ResourceWalker walker, @Nullable ResourceMatcher matcher) throws IOException {
        if(!resource.exists()) {
            return getMaximum();
        } else if(resource.isFile()) {
            return modifiedResource(resource);
        } else if(resource.isDirectory()) {
            return modifiedDirRec(resource, walker, matcher);
        }
        return getMinimum();
    }


    private static long modifiedResource(ReadableResource resource) throws IOException {
        return resource.getLastModifiedTime().toEpochMilli();
    }

    private static long modifiedDir(FSResource dir, @Nullable ResourceMatcher matcher) throws IOException {
        final long[] lastModified = {getMinimum()}; // Use array to allow access to non-final variable in closure.
        final boolean useMatcher = matcher != null;
        try(final Stream<? extends FSResource> stream = useMatcher ? dir.list(matcher) : dir.list()) {
            stream.forEach((resource) -> {
                try {
                    final long modified = modifiedResource(resource);
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
        final long[] lastModified = {getMinimum()}; // Use array to allow access to non-final variable in closure.
        final boolean useWalker = walker != null && matcher != null;
        try(final Stream<? extends FSResource> stream = useWalker ? dir.walk(walker, matcher) : dir.walk()) {
            stream.forEach((resource) -> {
                try {
                    final long modified = modifiedResource(resource);
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

    private static long getMinimum() {
        return Long.MIN_VALUE;
    }

    private static long getMaximum() {
        return Long.MAX_VALUE;
    }
}
