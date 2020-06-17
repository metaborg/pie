package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Objects;

public class NonIncrFunction<T extends Serializable, R extends @Nullable Serializable> implements Function<T, R> {
    private final java.util.function.Function<? super T, ? extends R> nonIncrFunction;

    public NonIncrFunction(java.util.function.Function<? super T, ? extends R> nonIncrFunction) {
        this.nonIncrFunction = nonIncrFunction;
    }

    @Override public R apply(ExecContext context, T input) {
        return nonIncrFunction.apply(input);
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final NonIncrFunction<?, ?> that = (NonIncrFunction<?, ?>)o;
        return nonIncrFunction.equals(that.nonIncrFunction);
    }

    @Override public int hashCode() {
        return Objects.hash(nonIncrFunction);
    }

    @Override public String toString() {
        return "NonIncrFunction(" + nonIncrFunction + ')';
    }
}
