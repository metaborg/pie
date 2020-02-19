package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.Serializable;
import java.util.function.Function;

public class MappedSupplier<T extends @Nullable Serializable, R extends @Nullable Serializable> implements Supplier<R> {
    private final Supplier<T> supplier;
    private final Function<? super T, ? extends R> func;

    public MappedSupplier(Supplier<T> supplier, Function<? super T, ? extends R> func) {
        this.supplier = supplier;
        this.func = func;
    }

    @Override public R get(ExecContext context) throws ExecException, IOException, InterruptedException {
        return func.apply(supplier.get(context));
    }
}
