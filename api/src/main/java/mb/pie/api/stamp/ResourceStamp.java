package mb.pie.api.stamp;

import mb.resource.Resource;

import java.io.Serializable;

/**
 * Stamp produced by a [ResourceStamper]. Stamps must be [Serializable].
 */
public interface ResourceStamp<R extends Resource> extends Serializable {
    ResourceStamper<R> getStamper();
}
