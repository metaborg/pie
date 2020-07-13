package mb.pie.api.stamp.output;

import mb.pie.api.stamp.OutputStamp;
import mb.pie.api.stamp.OutputStamper;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.function.Function;

/**
 * Output stamper that first applies given [func] to an output, copies the result of that into a stamp, and compares
 * these stamps by equality. Given [function][func] must be [Serializable].
 */
public class FuncEqualsOutputStamper implements OutputStamper {
    private final Function<@Nullable Serializable, @Nullable Serializable> func;

    public FuncEqualsOutputStamper(Function<@Nullable Serializable, @Nullable Serializable> func) {
        this.func = func;
    }


    @Override public OutputStamp stamp(@Nullable Serializable output) {
        final @Nullable Serializable value = func.apply(output);
        return new ValueOutputStamp<>(value, this);
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final FuncEqualsOutputStamper that = (FuncEqualsOutputStamper)o;
        return func.equals(that.func);
    }

    @Override public int hashCode() {
        return func.hashCode();
    }

    @Override public String toString() {
        return "FuncEqualsOutputStamper(" + func + ')';
    }
}
