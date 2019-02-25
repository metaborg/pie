package mb.pie.api;

/**
 * Resource system with a [unique identifier][id] that [resolves resource keys into resources][getResource].
 */
public interface ResourceSystem {
    String getId();

    Resource getResource(ResourceKey key);
}
