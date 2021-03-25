package mb.pie.api;

import mb.pie.api.stamp.output.OutputStampers;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class SupplierWithOrigins<T extends Serializable> implements Supplier<T> {
    private final Supplier<T> supplier;
    private final ArrayList<Supplier<?>> origins;

    public SupplierWithOrigins(Supplier<T> supplier, ArrayList<Supplier<?>> origins) {
        this.supplier = supplier;
        this.origins = origins;
    }

    public SupplierWithOrigins(Supplier<T> supplier, Collection<? extends Supplier<?>> origins) {
        this.supplier = supplier;
        this.origins = new ArrayList<>(origins);
    }

    public SupplierWithOrigins(Supplier<T> supplier, Iterable<? extends Supplier<?>> origins) {
        this.supplier = supplier;
        this.origins = new ArrayList<>();
        for(Supplier<?> origin : origins) {
            this.origins.add(origin);
        }
    }

    public SupplierWithOrigins(Supplier<T> supplier, Supplier<?>... origins) {
        this.supplier = supplier;
        this.origins = new ArrayList<>();
        Collections.addAll(this.origins, origins);
    }

    @Override public T get(ExecContext context) {
        origins.forEach(origin -> {
            if(origin instanceof STask<?>) {
                context.require((STask<?>)origin, OutputStampers.inconsequential());
            } else {
                context.require(origin);
            }
        });
        return context.require(supplier);
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final SupplierWithOrigins<?> that = (SupplierWithOrigins<?>)o;
        if(!supplier.equals(that.supplier)) return false;
        return origins.equals(that.origins);
    }

    @Override public int hashCode() {
        int result = supplier.hashCode();
        result = 31 * result + origins.hashCode();
        return result;
    }

    @Override public String toString() {
        return "SupplierWithOrigin{" +
            "supplier=" + supplier +
            ", origins=" + origins +
            '}';
    }
}
