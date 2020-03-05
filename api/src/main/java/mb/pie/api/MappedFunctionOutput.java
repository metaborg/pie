package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public class MappedFunctionOutput<T extends Serializable, R extends @Nullable Serializable, A extends @Nullable Serializable> implements Function<T, A> {
    private final Function<T, R> function;
    private final java.util.function.Function<? super R, ? extends A> after;

    public MappedFunctionOutput(Function<T, R> function, java.util.function.Function<? super R, ? extends A> after) {
        this.function = function;
        this.after = after;
    }

    @Override public A apply(ExecContext context, T input) throws ExecException, InterruptedException {
        final R result = function.apply(context, input);
        return after.apply(result);
    }
}
