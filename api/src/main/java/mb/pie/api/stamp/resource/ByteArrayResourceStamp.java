package mb.pie.api.stamp.resource;

import mb.pie.api.stamp.ResourceStamp;
import mb.pie.api.stamp.ResourceStamper;
import mb.resource.Resource;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Arrays;

public class ByteArrayResourceStamp<R extends Resource> implements ResourceStamp<R> {
    private final byte[] value;
    private final ResourceStamper<R> stamper;

    public ByteArrayResourceStamp(byte[] value, ResourceStamper<R> stamper) {
        this.value = value;
        this.stamper = stamper;
    }

    @Override public ResourceStamper<R> getStamper() {
        return stamper;
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final ByteArrayResourceStamp<?> that = (ByteArrayResourceStamp<?>) o;
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
