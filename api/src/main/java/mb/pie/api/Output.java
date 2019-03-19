package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Objects;

public final class Output<@Nullable O extends Serializable> {
    public final O output;

    public Output(O output) {
        this.output = output;
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final Output<?> that = (Output<?>) o;
        return Objects.equals(this.output, that.output);
    }

    @Override public int hashCode() {
        //noinspection ConstantConditions
        return output != null ? output.hashCode() : 0;
    }

    @Override public String toString() {
        return "Output(output=" + this.output + ")";
    }
}
