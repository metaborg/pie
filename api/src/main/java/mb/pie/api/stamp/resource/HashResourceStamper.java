package mb.pie.api.stamp.resource;

import mb.pie.api.stamp.ResourceStamper;
import mb.resource.ReadableResource;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;

public class HashResourceStamper<R extends ReadableResource> implements ResourceStamper<R> {
    @Override public ByteArrayResourceStamp<R> stamp(R resource) throws IOException {
        final Hash hasher = new Hash();
        hasher.updateFile(resource);
        final byte[] bytes = hasher.getHashBytesAndReset();
        return new ByteArrayResourceStamp<>(bytes, this);
    }

    @Override public boolean equals(@Nullable Object o) {
        return this == o || o != null && this.getClass() == o.getClass();
    }

    @Override public int hashCode() {
        return 0;
    }

    @Override public String toString() {
        return "HashResourceStamper()";
    }
}
