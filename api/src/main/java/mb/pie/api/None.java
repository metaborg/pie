package mb.pie.api;

import javax.annotation.Nullable;
import java.io.Serializable;

public class None implements Serializable {
    public static final None instance = new None();

    private None() {}

    public boolean equals(@Nullable Object other) {
        return this == other;
    }

    public int hashCode() {
        return 0;
    }

    public String toString() {
        return "None()";
    }
}
