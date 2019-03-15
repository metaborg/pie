package mb.pie.api.fs.stamp;

import mb.fs.api.node.FSNode;
import mb.fs.api.node.match.FSNodeMatcher;
import mb.pie.api.fs.FileSystemResource;
import mb.pie.api.stamp.ResourceStamp;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.security.MessageDigest;

public class HashResourceStamper implements HashResourceStamperTrait {
    private final @Nullable FSNodeMatcher matcher;


    public HashResourceStamper(FSNodeMatcher matcher) {
        this.matcher = matcher;
    }

    public HashResourceStamper() {
        this.matcher = null;
    }


    @Override public ResourceStamp<FileSystemResource> stamp(FileSystemResource resource) throws IOException {
        final FSNode node = resource.node;
        if(!node.exists()) {
            return new NullResourceStamp(this);
        }
        final MessageDigest digest = createDigester();
        update(digest, node, matcher);
        final byte[] bytes = digest.digest();
        return new ByteArrayResourceStamp(bytes, this);
    }


    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final HashResourceStamper that = (HashResourceStamper) o;
        return matcher != null ? matcher.equals(that.matcher) : that.matcher == null;
    }

    @Override public int hashCode() {
        return matcher != null ? matcher.hashCode() : 0;
    }

    @Override public String toString() {
        return "HashResourceStamper(" + matcher + ")";
    }
}
