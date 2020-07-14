package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

/**
 * Specialization of [OutTransient], where a serializable value [e] is used for change detection through equality.
 */
public interface OutTransientEquatable<T extends @Nullable Object, E extends @Nullable Serializable> extends OutTransient<T> {
    E getEquatableValue();
}
