package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public class OutTransientEquatableImpl<T extends @Nullable Object, E extends Serializable> implements OutTransientEquatable<T, E> {
    private final @Nullable T value;
    private final @Nullable E equatable;
    private final boolean consistent;

    public OutTransientEquatableImpl(T value, E equatable, boolean consistent) {
        this.value = value;
        this.equatable = equatable;
        this.consistent = consistent;
    }


    @Override public @Nullable T getValue() {
        return value;
    }

    @Override public @Nullable E getEquatableValue() {
        return equatable;
    }

    @Override public boolean isConsistent() {
        return consistent;
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final OutTransientEquatableImpl<?, ?> that = (OutTransientEquatableImpl<?, ?>) o;
        if(consistent != that.consistent) return false;
        if(value != null ? !value.equals(that.value) : that.value != null) return false;
        return equatable != null ? equatable.equals(that.equatable) : that.equatable == null;
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
