package mb.pie.util;

import java.io.Serializable;
import java.util.Objects;

public class Tuple4<T1 extends Serializable, T2 extends Serializable, T3 extends Serializable, T4 extends Serializable> implements Serializable {
    private final T1 f1;
    private final T2 f2;
    private final T3 f3;
    private final T4 f4;

    public Tuple4(T1 f1, T2 f2, T3 f3, T4 f4) {
        this.f1 = f1;
        this.f2 = f2;
        this.f3 = f3;
        this.f4 = f4;
    }

    public T1 getF1() {
        return f1;
    }

    public T2 getF2() {
        return f2;
    }

    public T3 getF3() {
        return f3;
    }

    public T4 getF4() {
        return f4;
    }

    public T1 component1() {
        return f1;
    }

    public T2 component2() {
        return f2;
    }

    public T3 component3() {
        return f3;
    }

    public T4 component4() {
        return f4;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tuple4)) return false;
        Tuple4<?, ?, ?, ?> tuple4 = (Tuple4<?, ?, ?, ?>) o;
        return Objects.equals(f1, tuple4.f1) &&
                Objects.equals(f2, tuple4.f2) &&
                Objects.equals(f3, tuple4.f3) &&
                Objects.equals(f4, tuple4.f4);
    }

    @Override
    public int hashCode() {
        return Objects.hash(f1, f2, f3, f4);
    }

    @Override public String toString() {
        return "(" + f1 + ", " + f2 + ", " + f3 + ", " + f4 + ")";
    }
}
