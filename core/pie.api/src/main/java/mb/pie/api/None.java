package mb.pie.api;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Serializable;

public class None implements Serializable {
    public static final None instance = new None();

    private None() {}

    public boolean equals(@Nullable Object other) {
        return this == other || other != null && this.getClass() == other.getClass();
    }

    public int hashCode() {
        return 0;
    }

    public String toString() {
        return "None()";
    }
}
