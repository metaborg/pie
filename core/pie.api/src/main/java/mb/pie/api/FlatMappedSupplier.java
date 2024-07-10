package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public class FlatMappedSupplier<T extends Serializable, R extends Serializable> implements Supplier<R> {
    private final Supplier<T> supplier;
    private final Function<? super T, Supplier<R>> func;

    public FlatMappedSupplier(Supplier<T> supplier, Function<? super T, Supplier<R>> func) {
        this.supplier = supplier;
        this.func = func;
    }

    @Override public R get(ExecContext context) {
        return func.apply(context, supplier.get(context)).get(context);
    }

    @Override public boolean equals(@Nullable Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final FlatMappedSupplier<?, ?> mappedSupplier = (FlatMappedSupplier<?, ?>)o;
        if(!supplier.equals(mappedSupplier.supplier)) return false;
        return func.equals(mappedSupplier.func);
    }

    @Override public int hashCode() {
        int result = supplier.hashCode();
        result = 31 * result + func.hashCode();
        return result;
    }

    @Override public String toString() {
        return "MappedSupplier(" + supplier + ", " + func + ")";
    }
}
