package mb.pie.api;

import mb.pie.api.stamp.ResourceStamper;
import mb.resource.ReadableResource;
import mb.resource.ResourceKey;

import java.io.IOException;
import java.util.Objects;

public class ResourceBytesProvider implements Provider<byte[]> {
    private final ResourceKey key;
    private final ResourceStamper<ReadableResource> stamper;

    public ResourceBytesProvider(ResourceKey key, ResourceStamper<ReadableResource> stamper) {
        this.key = key;
        this.stamper = stamper;
    }

    @Override public byte[] get(ExecContext context) throws IOException {
        return context.require(key, stamper).readBytes();
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final ResourceBytesProvider that = (ResourceBytesProvider)o;
        return key.equals(that.key) &&
            stamper.equals(that.stamper);
    }

    @Override public int hashCode() {
        return Objects.hash(key, stamper);
    }

    @Override public String toString() {
        return "ResourceBytesProvider{" +
            "key=" + key +
            ", stamper=" + stamper +
            '}';
    }
}
