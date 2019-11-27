package mb.pie.api.stamp.resource;

import mb.pie.api.stamp.ResourceStamper;
import mb.resource.ReadableResource;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;

public class ModifiedResourceStamper<R extends ReadableResource> implements ResourceStamper<R> {
    @Override public ValueResourceStamp<R> stamp(R file) throws IOException {
        final long modified = Modified.modified(file);
        return new ValueResourceStamp<>(modified, this);
    }

    @Override public boolean equals(@Nullable Object o) {
        return this == o || o != null && this.getClass() == o.getClass();
    }

    @Override public int hashCode() {
        return 0;
    }

    @Override public String toString() {
        return "ModifiedResourceStamper()";
    }
}
