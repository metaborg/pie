package mb.pie.api.fs.stamp;

import mb.pie.api.fs.FileSystemResource;
import mb.pie.api.stamp.ResourceStamp;
import mb.pie.api.stamp.ResourceStamper;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;

public class ExistsResourceStamper implements ResourceStamper<FileSystemResource> {
    @Override public ResourceStamp<FileSystemResource> stamp(FileSystemResource resource) throws IOException {
        return new ValueResourceStamp<>(resource.node.exists(), this);
    }


    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        return true;
    }

    @Override public int hashCode() {
        return 0;
    }

    @Override public String toString() {
        return "Exists";
    }
}
