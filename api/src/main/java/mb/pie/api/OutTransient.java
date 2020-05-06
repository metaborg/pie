package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

/**
 * Wrapper for transient outputs; outputs that cannot be serialized. A transient output will be recreated when an
 * attempt is made to deserialize it, and then cached.
 */
public interface OutTransient<T extends @Nullable Object> extends Serializable {
    T getValue();

    boolean isConsistent();
}
