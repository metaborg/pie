package mb.pie.api.stamp.resource;

import mb.pie.api.stamp.ResourceStamp;
import mb.pie.api.stamp.ResourceStamper;
import mb.resource.Resource;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.nio.charset.StandardCharsets;
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
        final ByteArrayResourceStamp<?> that = (ByteArrayResourceStamp<?>)o;
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
            "value=" + bytesToHex(value) +
            ", stamper=" + stamper +
            ')';
    }

    // From: https://stackoverflow.com/a/9855338/499240
    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);
    public static String bytesToHex(byte[] bytes) {
        byte[] hexChars = new byte[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }
}
