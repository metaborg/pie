package mb.pie.api.stamp.resource;

import mb.pie.api.stamp.ResourceStamper;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.match.ResourceMatcher;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.util.Objects;

public class HashMatchResourceStamper implements ResourceStamper<HierarchicalResource> {
    private final @Nullable ResourceMatcher matcher;

    public HashMatchResourceStamper(ResourceMatcher matcher) {
        this.matcher = matcher;
    }

    public HashMatchResourceStamper() {
        this.matcher = null;
    }

    @Override
    public ByteArrayResourceStamp<HierarchicalResource> stamp(HierarchicalResource resource) throws IOException {
        final Hash hasher = new Hash();
        hasher.update(resource, matcher);
        final byte[] bytes = hasher.getHashBytesAndReset();
        return new ByteArrayResourceStamp<>(bytes, this);
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final HashMatchResourceStamper that = (HashMatchResourceStamper)o;
        return Objects.equals(matcher, that.matcher);
    }

    @Override public int hashCode() {
        return matcher != null ? matcher.hashCode() : 0;
    }

    @Override public String toString() {
        return "HashMatchResourceStamper(" + matcher + ")";
    }
}
