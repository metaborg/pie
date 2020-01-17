package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.Serializable;
import java.util.function.Function;

public class MappedProvider<T extends @Nullable Serializable, R extends @Nullable Serializable> implements Provider<R> {
    private final Provider<T> provider;
    private final Function<T, R> func;

    public MappedProvider(Provider<T> provider, Function<T, R> func) {
        this.provider = provider;
        this.func = func;
    }

    @Override public R get(ExecContext context) throws ExecException, IOException, InterruptedException {
        //noinspection ConstantConditions
        return func.apply(provider.get(context));
    }
}
