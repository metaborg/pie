package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Collection of [resource systems][ResourceSystem].
 */
public interface ResourceSystems {
    @Nullable ResourceSystem getResourceSystem(String id);
}
