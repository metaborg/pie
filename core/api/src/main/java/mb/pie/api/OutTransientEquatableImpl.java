package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Objects;

public class OutTransientEquatableImpl<T extends @Nullable Object, E extends @Nullable Serializable> implements OutTransientEquatable<T, E> {
    private transient final T value;
    private final E equatable;
    private transient final boolean consistent;

    public OutTransientEquatableImpl(T value, E equatable, boolean consistent) {
        this.value = value;
        this.equatable = equatable;
        this.consistent = consistent;
    }


    @Override public T getValue() {
        return value;
    }

    @Override public E getEquatableValue() {
        return equatable;
    }

    @Override public boolean isConsistent() {
        return consistent;
    }


    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final OutTransientEquatableImpl<?, ?> that = (OutTransientEquatableImpl<?, ?>)o;
        if(consistent != that.consistent) return false;
        if(!Objects.equals(value, that.value)) return false;
        return Objects.equals(equatable, that.equatable);
    }

    @Override public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (equatable != null ? equatable.hashCode() : 0);
        result = 31 * result + (consistent ? 1 : 0);
        return result;
    }

    @Override public String toString() {
        return "OutTransientEquatableImpl{" +
            "value=" + value +
            ", equatable=" + equatable +
            ", consistent=" + consistent +
            '}';
    }
}
