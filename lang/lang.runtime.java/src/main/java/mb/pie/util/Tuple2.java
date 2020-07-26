package mb.pie.util;

import java.io.Serializable;
import java.util.Objects;

public class Tuple2<T1 extends Serializable, T2 extends Serializable> implements Serializable {
    private final T1 f1;
    private final T2 f2;

    public Tuple2(T1 f1, T2 f2) {
        this.f1 = f1;
        this.f2 = f2;
    }

    public T1 getF1() {
        return f1;
    }

    public T2 getF2() {
        return f2;
    }

    public T1 component1() {
        return f1;
    }

    public T2 component2() {
        return f2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tuple2)) return false;
        Tuple2<?, ?> tuple2 = (Tuple2<?, ?>) o;
        return Objects.equals(f1, tuple2.f1) &&
                Objects.equals(f2, tuple2.f2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(f1, f2);
    }

    @Override public String toString() {
        return "(" + f1 + ", " + f2 + ")";
    }
}
