package mb.pie.api;

import mb.pie.api.stamp.output.OutputStampers;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public class SupplierWithOrigin<T extends Serializable> implements Supplier<T> {
    private final Supplier<T> supplier;
    private final Supplier<?> origin;

    public SupplierWithOrigin(Supplier<T> supplier, Supplier<?> origin) {
        this.supplier = supplier;
        this.origin = origin;
    }

    @Override public T get(ExecContext context) {
        if(origin instanceof STask<?>) {
            context.require((STask<?>)origin, OutputStampers.inconsequential());
        } else {
            context.require(origin);
        }
        return context.require(supplier);
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final SupplierWithOrigin<?> that = (SupplierWithOrigin<?>)o;
        if(!supplier.equals(that.supplier)) return false;
        return origin.equals(that.origin);
    }

    @Override public int hashCode() {
        int result = supplier.hashCode();
        result = 31 * result + origin.hashCode();
        return result;
    }

    @Override public String toString() {
        return "SupplierWithOrigin{" +
            "supplier=" + supplier +
            ", origin=" + origin +
            '}';
    }
}
