package mb.pie.api.fs.stamp;

import mb.pie.api.fs.FileSystemResource;
import mb.pie.api.stamp.ResourceStamp;
import mb.pie.api.stamp.ResourceStamper;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ValueResourceStamp<V> implements ResourceStamp<FileSystemResource> {
    private final @Nullable V value;
    private final ResourceStamper<FileSystemResource> stamper;


    public ValueResourceStamp(@Nullable V value, ResourceStamper<FileSystemResource> stamper) {
        this.value = value;
        this.stamper = stamper;
    }


    @Override public ResourceStamper<FileSystemResource> getStamper() {
        return stamper;
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final ValueResourceStamp<?> that = (ValueResourceStamp<?>) o;
        if(value != null ? !value.equals(that.value) : that.value != null) return false;
        return stamper.equals(that.stamper);
    }

    @Override public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + stamper.hashCode();
        return result;
    }

    @Override public String toString() {
        return "ValueResourceStamp(value=" + value + ", stamper=" + stamper + ')';
    }
}
