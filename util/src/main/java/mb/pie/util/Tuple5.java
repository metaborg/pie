package mb.pie.util;

import java.util.Objects;

public class Tuple5<T1, T2, T3, T4, T5> {
    private final T1 f1;
    private final T2 f2;
    private final T3 f3;
    private final T4 f4;
    private final T5 f5;

    public Tuple5(T1 f1, T2 f2, T3 f3, T4 f4, T5 f5) {
        this.f1 = f1;
        this.f2 = f2;
        this.f3 = f3;
        this.f4 = f4;
        this.f5 = f5;
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

    public T5 getF5() {
        return f5;
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

    public T5 component5() {
        return f5;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tuple5)) return false;
        Tuple5<?, ?, ?, ?, ?> tuple5 = (Tuple5<?, ?, ?, ?, ?>) o;
        return Objects.equals(f1, tuple5.f1) &&
                Objects.equals(f2, tuple5.f2) &&
                Objects.equals(f3, tuple5.f3) &&
                Objects.equals(f4, tuple5.f4) &&
                Objects.equals(f5, tuple5.f5);
    }

    @Override
    public int hashCode() {
        return Objects.hash(f1, f2, f3, f4, f5);
    }
}