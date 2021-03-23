package mb.pie.api;

import java.io.Serializable;

public class SupplierWithOrigin<T extends Serializable> implements Supplier<T> {
    private final Supplier<T> supplier;
    private final Supplier<?> origin;

    public SupplierWithOrigin(Supplier<T> supplier, Supplier<?> origin) {
        this.supplier = supplier;
        this.origin = origin;
    }

    @Override public T get(ExecContext context) {
        context.require(origin);
        return context.require(supplier);
    }
}
