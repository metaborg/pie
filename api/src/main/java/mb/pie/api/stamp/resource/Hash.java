package mb.pie.api.stamp.resource;

import mb.resource.ReadableResource;
import mb.resource.fs.FSResource;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.stream.Stream;

public class Hash {
    private final MessageDigest digest;

    Hash() {
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch(NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    void update(FSResource resource, @Nullable ResourceMatcher matcher) throws IOException {
        if(resource.isFile()) {
            updateFile(resource);
        }
        if(resource.isDirectory()) {
            updateDir(resource, matcher);
        }
    }

    void updateRec(FSResource resource, @Nullable ResourceWalker walker, @Nullable ResourceMatcher matcher) throws IOException {
        if(resource.isFile()) {
            updateFile(resource);
        }
        if(resource.isDirectory()) {
            updateDirRec(resource, walker, matcher);
        }
    }

    void updateFile(ReadableResource file) throws IOException {
        digest.update(file.readBytes());
    }

    void updateDir(FSResource dir, @Nullable ResourceMatcher matcher) throws IOException {
        final boolean useMatcher = matcher != null;
        try(final Stream<FSResource> stream = useMatcher ? dir.list(matcher) : dir.list()) {
            stream.forEach((resource) -> {
                try {
                    if(resource.isFile()) {
                        updateFile(resource);
                    }
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch(UncheckedIOException e) {
            throw e.getCause();
        }
    }

    void updateDirRec(FSResource dir, @Nullable ResourceWalker walker, @Nullable ResourceMatcher matcher) throws IOException {
        final boolean useWalker = walker != null && matcher != null;
        try(final Stream<FSResource> stream = useWalker ? dir.walk(walker, matcher) : dir.walk()) {
            stream.forEach((resource) -> {
                try {
                    if(resource.isFile()) {
                        updateFile(resource);
                    }
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch(UncheckedIOException e) {
            throw e.getCause();
        }
    }

    byte[] getHashBytesAndReset() {
        return digest.digest();
    }
}
