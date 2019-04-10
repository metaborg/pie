package mb.pie.api.stamp.resource;

import mb.pie.api.stamp.ResourceStamper;
import mb.resource.fs.FSResource;
import mb.resource.fs.match.ResourceMatcher;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.util.Objects;

public class ModifiedMatchResourceStamper implements ResourceStamper<FSResource> {
    private final @Nullable ResourceMatcher matcher;

    public ModifiedMatchResourceStamper(ResourceMatcher matcher) {
        this.matcher = matcher;
    }

    public ModifiedMatchResourceStamper() {
        this.matcher = null;
    }

    @Override public ValueResourceStamp<FSResource> stamp(FSResource resource) throws IOException {
        final long modified = Modified.modified(resource, matcher);
        return new ValueResourceStamp<>(modified, this);
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final ModifiedMatchResourceStamper that = (ModifiedMatchResourceStamper) o;
        return Objects.equals(matcher, that.matcher);
    }

    @Override public int hashCode() {
        return matcher != null ? matcher.hashCode() : 0;
    }

    @Override public String toString() {
        return "ModifiedMatchResourceStamper(" + matcher + ")";
    }
}
