package mb.pie.api.stamp.resource;

import mb.resource.ReadableResource;
import mb.resource.hierarchical.HierarchicalResource;
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


    void update(ReadableResource resource) throws IOException {
        if(!resource.exists()) {
            updateNonExistent();
        } else {
            updateResource(resource);
        }
    }

    void update(HierarchicalResource resource, @Nullable ResourceMatcher matcher) throws IOException {
        if(!resource.exists()) {
            updateNonExistent();
        } else if(resource.isFile()) {
            updateResource(resource);
        } else if(resource.isDirectory()) {
            updateDir(resource, matcher);
        }
    }

    void updateRec(HierarchicalResource resource, @Nullable ResourceWalker walker, @Nullable ResourceMatcher matcher) throws IOException {
        if(!resource.exists()) {
            updateNonExistent();
        } else if(resource.isFile()) {
            updateResource(resource);
        } else if(resource.isDirectory()) {
            updateDirRec(resource, walker, matcher);
        }
    }


    private void updateNonExistent() {
        digest.update((byte) 0);
    }

    private void updateResource(ReadableResource file) throws IOException {
        digest.update((byte) 1);
        digest.update(file.readBytes());
    }

    private void updateDir(HierarchicalResource dir, @Nullable ResourceMatcher matcher) throws IOException {
        final boolean useMatcher = matcher != null;
        try(final Stream<? extends HierarchicalResource> stream = useMatcher ? dir.list(matcher) : dir.list()) {
            stream.forEach((resource) -> {
                try {
                    updateResource(resource);
                } catch(IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch(UncheckedIOException e) {
            throw e.getCause();
        }
    }

    private void updateDirRec(HierarchicalResource dir, @Nullable ResourceWalker walker, @Nullable ResourceMatcher matcher) throws IOException {
        final boolean useWalker = walker != null && matcher != null;
        try(final Stream<? extends HierarchicalResource> stream = useWalker ? dir.walk(walker, matcher) : dir.walk()) {
            stream.forEach((resource) -> {
                try {
                    updateResource(resource);
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
