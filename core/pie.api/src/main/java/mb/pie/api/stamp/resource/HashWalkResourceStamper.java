package mb.pie.api.stamp.resource;

import mb.pie.api.stamp.ResourceStamper;
import mb.resource.hierarchical.HierarchicalResource;
import mb.resource.hierarchical.match.ResourceMatcher;
import mb.resource.hierarchical.walk.ResourceWalker;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.util.Objects;

public class HashWalkResourceStamper implements ResourceStamper<HierarchicalResource> {
    private final @Nullable ResourceWalker walker;
    private final @Nullable ResourceMatcher matcher;

    public HashWalkResourceStamper(ResourceWalker walker, ResourceMatcher matcher) {
        this.walker = walker;
        this.matcher = matcher;
    }

    public HashWalkResourceStamper() {
        this.walker = null;
        this.matcher = null;
    }

    @Override
    public ByteArrayResourceStamp<HierarchicalResource> stamp(HierarchicalResource resource) throws IOException {
        final Hash hasher = new Hash();
        hasher.updateRec(resource, walker, matcher);
        final byte[] bytes = hasher.getHashBytesAndReset();
        return new ByteArrayResourceStamp<>(bytes, this);
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final HashWalkResourceStamper that = (HashWalkResourceStamper)o;
        if(!Objects.equals(walker, that.walker)) return false;
        return Objects.equals(matcher, that.matcher);
    }

    @Override public int hashCode() {
        int result = walker != null ? walker.hashCode() : 0;
        result = 31 * result + (matcher != null ? matcher.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "HashWalkResourceStamper(" + walker + ", " + matcher + ")";
    }
}
