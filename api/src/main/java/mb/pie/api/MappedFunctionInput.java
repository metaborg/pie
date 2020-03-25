package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public class MappedFunctionInput<B extends Serializable, T extends Serializable, R extends @Nullable Serializable> implements Function<B, R> {
    private final Function<T, R> function;
    private final java.util.function.Function<? super B, ? extends T> before;

    public MappedFunctionInput(Function<T, R> function, java.util.function.Function<? super B, ? extends T> before) {
        this.function = function;
        this.before = before;
    }

    @Override public R apply(ExecContext context, B input) throws ExecException, InterruptedException {
        final T newInput = before.apply(input);
        return function.apply(context, newInput);
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final MappedFunctionInput<?, ?, ?> mappedFunctionInput = (MappedFunctionInput<?, ?, ?>)o;
        if(!function.equals(mappedFunctionInput.function)) return false;
        return before.equals(mappedFunctionInput.before);
    }

    @Override public int hashCode() {
        int result = function.hashCode();
        result = 31 * result + before.hashCode();
        return result;
    }

    @Override public String toString() {
        return "MappedFunctionInput(" + function + ", " + before + ")";
    }
}
