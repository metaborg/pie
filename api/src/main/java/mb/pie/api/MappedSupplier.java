package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.Serializable;

public class MappedSupplier<T extends Serializable, R extends @Nullable Serializable> implements Supplier<R> {
    private final Supplier<T> supplier;
    private final Function<? super T, ? extends R> func;

    public MappedSupplier(Supplier<T> supplier, Function<? super T, ? extends R> func) {
        this.supplier = supplier;
        this.func = func;
    }

    @Override public R get(ExecContext context) throws ExecException, IOException, InterruptedException {
        return func.apply(context, supplier.get(context));
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final MappedSupplier<?, ?> mappedSupplier = (MappedSupplier<?, ?>)o;
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
