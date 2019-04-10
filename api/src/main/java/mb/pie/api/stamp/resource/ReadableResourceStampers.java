package mb.pie.api.stamp.resource;

import mb.resource.ReadableResource;

/**
 * Common resource stampers for {@link ReadableResource}s.
 */
public class ReadableResourceStampers {
    public static <R extends ReadableResource> ExistsResourceStamper<R> exists() {
        return new ExistsResourceStamper<>();
    }

    public static <R extends ReadableResource> ModifiedResourceStamper<R> modified() {
        return new ModifiedResourceStamper<>();
    }

    public static <R extends ReadableResource> HashResourceStamper<R> hash() {
        return new HashResourceStamper<>();
    }
}
