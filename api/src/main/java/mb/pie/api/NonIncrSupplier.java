package mb.pie.api;

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

public class NonIncrSupplier<T extends Serializable> implements Supplier<T> {
    private final java.util.function.Supplier<T> nonIncrSupplier;

    public NonIncrSupplier(java.util.function.Supplier<T> nonIncrSupplier) {
        this.nonIncrSupplier = nonIncrSupplier;
    }

    @Override public T get(ExecContext context) throws ExecException, IOException, InterruptedException {
        return nonIncrSupplier.get();
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final NonIncrSupplier<?> that = (NonIncrSupplier<?>)o;
        return nonIncrSupplier.equals(that.nonIncrSupplier);
    }

    @Override public int hashCode() {
        return Objects.hash(nonIncrSupplier);
    }

    @Override public String toString() {
        return "NonIncrSupplier(" + nonIncrSupplier + ')';
    }
}
