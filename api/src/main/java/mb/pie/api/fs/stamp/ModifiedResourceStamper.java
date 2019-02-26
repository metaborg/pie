package mb.pie.api.fs.stamp;

import mb.fs.api.node.FSNode;
import mb.fs.api.node.match.FSNodeMatcher;
import mb.pie.api.fs.FileSystemResource;
import mb.pie.api.stamp.ResourceStamp;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;

public class ModifiedResourceStamper implements ModifiedResourceStamperTrait {
    private final @Nullable FSNodeMatcher matcher;


    public ModifiedResourceStamper(FSNodeMatcher matcher) {
        this.matcher = matcher;
    }

    public ModifiedResourceStamper() {
        this.matcher = null;
    }


    @Override public ResourceStamp<FileSystemResource> stamp(FileSystemResource resource) throws IOException {
        final FSNode node = resource.node;
        if(!node.exists()) {
            return new NullResourceStamp(this);
        }
        final long modified = modified(node, matcher);
        return new ValueResourceStamp<>(modified, this);
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final ModifiedResourceStamper that = (ModifiedResourceStamper) o;
        return matcher != null ? matcher.equals(that.matcher) : that.matcher == null;
    }

    @Override public int hashCode() {
        return matcher != null ? matcher.hashCode() : 0;
    }

    @Override public String toString() {
        return "ModifiedStamper(" + matcher + ")";
    }
}
