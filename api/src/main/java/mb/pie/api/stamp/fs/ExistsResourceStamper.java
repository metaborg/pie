package mb.pie.api.stamp.fs;

import mb.pie.api.stamp.ResourceStamper;
import mb.resource.fs.FSResource;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ExistsResourceStamper implements ResourceStamper<FSResource> {
    @Override public ValueResourceStamp<FSResource> stamp(FSResource resource) {
        return new ValueResourceStamp<>(resource.exists(), this);
    }

    @Override public boolean equals(@Nullable Object o) {
        return this == o || o != null && this.getClass() == o.getClass();
    }

    @Override public int hashCode() {
        return 0;
    }

    @Override public String toString() {
        return "ExistsResourceStamper()";
    }
}
