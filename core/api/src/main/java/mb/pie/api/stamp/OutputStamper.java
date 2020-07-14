package mb.pie.api.stamp;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

/**
 * Stamper for customizable change detection on task outputs. Stampers must be [Serializable].
 */
public interface OutputStamper extends Serializable {
    OutputStamp stamp(@Nullable Serializable output);
}
