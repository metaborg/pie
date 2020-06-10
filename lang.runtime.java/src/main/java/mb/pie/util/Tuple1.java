package mb.pie.util;

import java.io.Serializable;
import java.util.Objects;

public class Tuple1<T1 extends Serializable> implements Serializable {
    private final T1 f1;

    public Tuple1(T1 f1) {
        this.f1 = f1;
    }

    public T1 getF1() {
        return f1;
    }

    public T1 component1() {
        return f1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tuple1)) return false;
        Tuple1<?> tuple1 = (Tuple1<?>) o;
        return Objects.equals(f1, tuple1.f1);
    }

    @Override
    public int hashCode() {
        return Objects.hash(f1);
    }

    @Override public String toString() {
        return "(" + f1 + ")";
    }
}
