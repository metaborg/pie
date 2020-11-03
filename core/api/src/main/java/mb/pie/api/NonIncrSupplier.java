package mb.pie.api;

import java.io.Serializable;
import java.util.Objects;

public class NonIncrSupplier<T extends Serializable> implements Supplier<T> {
    private final SerializableSupplier<T> nonIncrSupplier;

    public NonIncrSupplier(SerializableSupplier<T> nonIncrSupplier) {
        this.nonIncrSupplier = nonIncrSupplier;
    }

    @Override public T get(ExecContext context) {
        return nonIncrSupplier.get();
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final NonIncrSupplier<?> that = (NonIncrSupplier<?>) o;
        return nonIncrSupplier.equals(that.nonIncrSupplier);
    }

    @Override public int hashCode() {
        return Objects.hash(nonIncrSupplier);
    }

    @Override public String toString() {
        return "NonIncrSupplier(" + nonIncrSupplier + ')';
    }
}
