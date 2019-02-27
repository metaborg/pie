package mb.pie.api.fs.stamp;

import mb.pie.api.fs.FileSystemResource;
import mb.pie.api.stamp.ResourceStamp;
import mb.pie.api.stamp.ResourceStamper;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Arrays;

public class ByteArrayResourceStamp implements ResourceStamp<FileSystemResource> {
    private final byte[] value;
    private final ResourceStamper<FileSystemResource> stamper;


    public ByteArrayResourceStamp(byte[] value, ResourceStamper<FileSystemResource> stamper) {
        this.value = value;
        this.stamper = stamper;
    }


    @Override public ResourceStamper<FileSystemResource> getStamper() {
        return stamper;
    }


    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final ByteArrayResourceStamp that = (ByteArrayResourceStamp) o;
        if(!Arrays.equals(value, that.value)) return false;
        return stamper.equals(that.stamper);
    }

    @Override public int hashCode() {
        int result = Arrays.hashCode(value);
        result = 31 * result + stamper.hashCode();
        return result;
    }

    @Override public String toString() {
        return "ByteArrayResourceStamp(" +
            "value=" + javax.xml.bind.DatatypeConverter.printHexBinary(value) +
            ", stamper=" + stamper +
            ')';
    }
}
