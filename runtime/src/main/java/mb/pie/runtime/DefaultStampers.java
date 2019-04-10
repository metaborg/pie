package mb.pie.runtime;

import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamper;
import mb.resource.ReadableResource;
import mb.resource.fs.FSResource;

public class DefaultStampers {
    public final OutputStamper output;

    public final ResourceStamper<ReadableResource> requireReadableResource;
    public final ResourceStamper<ReadableResource> provideReadableResource;

    public final ResourceStamper<FSResource> requireFSResource;
    public final ResourceStamper<FSResource> provideFSResource;

    public DefaultStampers(
        OutputStamper output,
        ResourceStamper<ReadableResource> requireReadableResource,
        ResourceStamper<ReadableResource> provideReadableResource,
        ResourceStamper<FSResource> requireFSResource,
        ResourceStamper<FSResource> provideFSResource
    ) {
        this.output = output;
        this.requireReadableResource = requireReadableResource;
        this.provideReadableResource = provideReadableResource;
        this.requireFSResource = requireFSResource;
        this.provideFSResource = provideFSResource;
    }

    @Override public String toString() {
        return "DefaultStampers(" +
            "output=" + output +
            ", requireReadableResource=" + requireReadableResource +
            ", provideReadableResource=" + provideReadableResource +
            ", requireFSResource=" + requireFSResource +
            ", provideFSResource=" + provideFSResource +
            ')';
    }
}
