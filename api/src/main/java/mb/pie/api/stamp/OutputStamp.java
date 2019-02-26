package mb.pie.api.stamp;

import java.io.Serializable;

/**
 * Stamp produced by an [OutputStamper]. Stamps must be [Serializable].
 */
public interface OutputStamp extends Serializable {
    OutputStamper getStamper();
}
