package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.Serializable;

public class FunctionBasedSupplier<T extends Serializable, R extends @Nullable Serializable> implements Supplier<R> {
    private final Function<T, R> function;
    private final T input;

    public FunctionBasedSupplier(Function<T, R> function, T input) {
        this.function = function;
        this.input = input;
    }

    @Override public R get(ExecContext context) {
        return function.apply(context, input);
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final FunctionBasedSupplier<?, ?> mappedFunctionInput = (FunctionBasedSupplier<?, ?>)o;
        if(!function.equals(mappedFunctionInput.function)) return false;
        return input.equals(mappedFunctionInput.input);
    }

    @Override public int hashCode() {
        int result = function.hashCode();
        result = 31 * result + input.hashCode();
        return result;
    }

    @Override public String toString() {
        return "FunctionBasedSupplier(" + function + ", " + input + ")";
    }
}
