package mb.pie.api;

import mb.pie.api.stamp.ResourceStamper;
import mb.resource.ReadableResource;
import mb.resource.ResourceKey;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class ResourceStringSupplier implements Supplier<String> {
    public final ResourceKey key;
    public final @Nullable ResourceStamper<ReadableResource> stamper;
    public final String charsetName;
    private transient Charset charset;

    public ResourceStringSupplier(ResourceKey key, @Nullable ResourceStamper<ReadableResource> stamper, Charset charset) {
        this.key = key;
        this.stamper = stamper;
        this.charsetName = charset.name();
        this.charset = charset;
    }

    public ResourceStringSupplier(ResourceKey key, @Nullable ResourceStamper<ReadableResource> stamper) {
        this(key, stamper, StandardCharsets.UTF_8);
    }

    public ResourceStringSupplier(ResourceKey key, Charset charset) {
        this(key, null, charset);
    }

    public ResourceStringSupplier(ResourceKey key) {
        this(key, (ResourceStamper<ReadableResource>)null);
    }


    @Override public String get(ExecContext context) {
        try {
            return context.require(key, stamper != null ? stamper : context.getDefaultRequireReadableResourceStamper()).readString(charset);
        } catch(IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final ResourceStringSupplier that = (ResourceStringSupplier)o;
        return key.equals(that.key) &&
            Objects.equals(stamper, that.stamper) &&
            charset.equals(that.charset);
    }

    @Override public int hashCode() {
        return Objects.hash(key, stamper, charset);
    }

    @Override public String toString() {
        return "ResourceStringSupplier{" +
            "key=" + key +
            ", stamper=" + stamper +
            ", charset=" + charset +
            '}';
    }


    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        in.defaultReadObject();
        this.charset = Charset.forName(charsetName);
    }
}
