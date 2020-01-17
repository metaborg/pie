package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Objects;

public class ValueProvider<T extends @Nullable Serializable> implements Provider<T> {
    private final T value;

    public ValueProvider(T value) {
        this.value = value;
    }

    @Override public T get(ExecContext context) {
        return value;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final ValueProvider<?> that = (ValueProvider<?>)o;
        return Objects.equals(value, that.value);
    }

    @Override public int hashCode() {
        return Objects.hash(value);
    }

    @Override public String toString() {
        return Objects.toString(value);
    }
}
