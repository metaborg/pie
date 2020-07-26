package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public class MappedFunctionOutput<T extends Serializable, R extends @Nullable Serializable, A extends @Nullable Serializable> implements Function<T, A> {
    private final Function<T, R> function;
    private final Function<? super R, ? extends A> after;

    public MappedFunctionOutput(Function<T, R> function, Function<? super R, ? extends A> after) {
        this.function = function;
        this.after = after;
    }

    @Override public A apply(ExecContext context, T input) {
        final R result = function.apply(context, input);
        return after.apply(context, result);
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final MappedFunctionOutput<?, ?, ?> mappedFunctionOutput = (MappedFunctionOutput<?, ?, ?>)o;
        if(!function.equals(mappedFunctionOutput.function)) return false;
        return after.equals(mappedFunctionOutput.after);
    }

    @Override public int hashCode() {
        int result = function.hashCode();
        result = 31 * result + after.hashCode();
        return result;
    }

    @Override public String toString() {
        return "MappedFunctionOutput(" + function + ", " + after + ")";
    }
}
