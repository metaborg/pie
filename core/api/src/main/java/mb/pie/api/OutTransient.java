package mb.pie.api;

import java.io.Serializable;

/**
 * Wrapper for transient outputs; outputs that cannot be serialized. A transient output will be recreated when an
 * attempt is made to deserialize it while {@link #isConsistent()} is {@code false}.
 *
 * @param <T> Type of transient output.
 */
public interface OutTransient<T> extends Serializable {
    /**
     * Gets the value of this transient output.
     *
     * @return Value of this transient output. May be {@code null}.
     */
    T getValue();

    /**
     * Gets whether this transient value is consistent. If false and deserialized as part of a task output, the task
     * will be re-executed to recreate this value.
     *
     * @return Whether this transient value is consistent.
     */
    boolean isConsistent();
}
