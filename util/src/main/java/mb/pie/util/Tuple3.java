package mb.pie.util;

import java.util.Objects;

public class Tuple3<T1, T2, T3> {
    private final T1 f1;
    private final T2 f2;
    private final T3 f3;

    public Tuple3(T1 f1, T2 f2, T3 f3) {
        this.f1 = f1;
        this.f2 = f2;
        this.f3 = f3;
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

    public T1 component1() {
        return f1;
    }

    public T2 component2() {
        return f2;
    }

    public T3 component3() {
        return f3;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tuple3)) return false;
        Tuple3<?, ?, ?> tuple3 = (Tuple3<?, ?, ?>) o;
        return Objects.equals(f1, tuple3.f1) &&
                Objects.equals(f2, tuple3.f2) &&
                Objects.equals(f3, tuple3.f3);
    }

    @Override
    public int hashCode() {
        return Objects.hash(f1, f2, f3);
    }
}
