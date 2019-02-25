package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

public class OutTransientImpl<T extends @Nullable Object> implements OutTransient<T> {
    private final @Nullable T value;
    private final boolean consistent;

    public OutTransientImpl(T value, boolean consistent) {
        this.value = value;
        this.consistent = consistent;
    }


    @Override public @Nullable T getValue() {
        return value;
    }

    @Override public boolean isConsistent() {
        return consistent;
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final OutTransientImpl<?> that = (OutTransientImpl<?>) o;
        if(consistent != that.consistent) return false;
        return value != null ? value.equals(that.value) : that.value == null;
    }

    @Override public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (consistent ? 1 : 0);
        return result;
    }

    @Override public String toString() {
        return "OutTransientImpl{" +
            "value=" + value +
            ", consistent=" + consistent +
            '}';
    }
}
