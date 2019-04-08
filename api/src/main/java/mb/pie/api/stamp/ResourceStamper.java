package mb.pie.api.stamp;

import mb.resource.Resource;

import java.io.IOException;
import java.io.Serializable;

/**
 * Stamper for customizable change detection on resources. Stampers must be [Serializable].
 */
public interface ResourceStamper<R extends Resource> extends Serializable {
    ResourceStamp<R> stamp(R resource) throws IOException;
}
