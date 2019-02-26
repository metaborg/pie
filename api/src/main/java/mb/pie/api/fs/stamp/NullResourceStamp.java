package mb.pie.api.fs.stamp;

import mb.pie.api.fs.FileSystemResource;
import mb.pie.api.stamp.ResourceStamp;
import mb.pie.api.stamp.ResourceStamper;

public class NullResourceStamp implements ResourceStamp<FileSystemResource> {
    private final ResourceStamper<FileSystemResource> stamper;


    public NullResourceStamp(ResourceStamper<FileSystemResource> stamper) {
        this.stamper = stamper;
    }


    @Override public ResourceStamper<FileSystemResource> getStamper() {
        return stamper;
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final NullResourceStamp that = (NullResourceStamp) o;
        return stamper.equals(that.stamper);
    }

    @Override public int hashCode() {
        return stamper.hashCode();
    }

    @Override public String toString() {
        return "NullResourceStamp(" + stamper + ')';
    }
}
