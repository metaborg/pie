package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

/**
 * Implementation of {@link SerializableFunction} that implements {@link #equals} and {@link #hashCode} only by
 * comparing its class. Implementing this base class with state will cause incrementality inconsistencies.
 */
public abstract class StatelessSerializableFunction<T, R extends Serializable> implements SerializableFunction<T, R> {
    @Override public boolean equals(@Nullable Object other) {
        return this == other || other != null && this.getClass() == other.getClass();
    }

    @Override public int hashCode() { return 0; }

    @Override public String toString() { return getClass().getSimpleName(); }
}
