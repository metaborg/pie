package mb.pie.api;

/**
 * Mutable resource with a [key] that uniquely identifies the resource.
 */
public interface Resource {
    ResourceKey key();
}
