package mb.pie.api.stamp.fs;

import mb.pie.api.stamp.ResourceStamper;
import mb.resource.fs.FSResource;
import mb.resource.fs.match.ResourceMatcher;
import mb.resource.fs.walk.ResourceWalker;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.util.Objects;

public class ModifiedWalkResourceStamper implements ResourceStamper<FSResource> {
    private final @Nullable ResourceWalker walker;
    private final @Nullable ResourceMatcher matcher;

    public ModifiedWalkResourceStamper(ResourceWalker walker, ResourceMatcher matcher) {
        this.walker = walker;
        this.matcher = matcher;
    }

    public ModifiedWalkResourceStamper() {
        this.walker = null;
        this.matcher = null;
    }

    @Override public ValueResourceStamp<FSResource> stamp(FSResource resource) throws IOException {
        final long modified = Modified.modifiedRec(resource, walker, matcher);
        return new ValueResourceStamp<>(modified, this);
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final ModifiedWalkResourceStamper that = (ModifiedWalkResourceStamper) o;
        if(!Objects.equals(walker, that.walker)) return false;
        return Objects.equals(matcher, that.matcher);
    }

    @Override public int hashCode() {
        int result = walker != null ? walker.hashCode() : 0;
        result = 31 * result + (matcher != null ? matcher.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "ModifiedWalkResourceStamper(" + walker + ")";
    }
}
