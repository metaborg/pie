package mb.pie.api;

import java.io.Serializable;

/**
 * Specialization of {@link OutTransient}, where a serializable value with identity of type {@link E} is used for change
 * detection through equality.
 *
 * @param <T> Type of transient output.
 * @param <E> Type of equatable value. Must implement {@link Serializable} and have identity (i.e., implement {@link
 *            Object#equals(Object)} and {@link Object#hashCode()}).
 */
public interface OutTransientEquatable<T, E extends Serializable> extends OutTransient<T> {
    /**
     * Gets the equatable value.
     *
     * @return Equatable value.
     */
    E getEquatableValue();
}
