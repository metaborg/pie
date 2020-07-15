package mb.pie.runtime;

import mb.pie.api.stamp.OutputStamper;
import mb.pie.api.stamp.ResourceStamper;
import mb.resource.ReadableResource;
import mb.resource.hierarchical.HierarchicalResource;

public class DefaultStampers {
    public final OutputStamper output;

    public final ResourceStamper<ReadableResource> requireReadableResource;
    public final ResourceStamper<ReadableResource> provideReadableResource;

    public final ResourceStamper<HierarchicalResource> requireHierarchicalResource;
    public final ResourceStamper<HierarchicalResource> provideHierarchicalResource;

    public DefaultStampers(
        OutputStamper output,
        ResourceStamper<ReadableResource> requireReadableResource,
        ResourceStamper<ReadableResource> provideReadableResource,
        ResourceStamper<HierarchicalResource> requireHierarchicalResource,
        ResourceStamper<HierarchicalResource> provideHierarchicalResource
    ) {
        this.output = output;
        this.requireReadableResource = requireReadableResource;
        this.provideReadableResource = provideReadableResource;
        this.requireHierarchicalResource = requireHierarchicalResource;
        this.provideHierarchicalResource = provideHierarchicalResource;
    }

    @Override public String toString() {
        return "DefaultStampers(" +
            "output=" + output +
            ", requireReadableResource=" + requireReadableResource +
            ", provideReadableResource=" + provideReadableResource +
            ", requireHierarchicalResource=" + requireHierarchicalResource +
            ", provideHierarchicalResource=" + provideHierarchicalResource +
            ')';
    }
}
