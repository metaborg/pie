package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

// TODO: replace with Option<O>
public final class Output<@Nullable O extends Serializable> {
    public final @Nullable O output;

    public Output(@Nullable O output) {
        this.output = output;
    }


    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final Output<?> output1 = (Output<?>) o;
        return output != null ? output.equals(output1.output) : output1.output == null;
    }

    @Override public int hashCode() {
        return output != null ? output.hashCode() : 0;
    }

    @Override public String toString() {
        return "Output(output=" + this.output + ")";
    }
}
