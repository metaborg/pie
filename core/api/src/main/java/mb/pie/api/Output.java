package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;
import java.util.Objects;

public final class Output implements Serializable {
    public final @Nullable Serializable output;

    public Output(@Nullable Serializable output) {
        this.output = output;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        final Output that = (Output)o;
        return Objects.equals(this.output, that.output);
    }

    @Override public int hashCode() {
        return output != null ? output.hashCode() : 0;
    }

    @Override public String toString() {
        return "Output(output=" + this.output + ")";
    }
}
