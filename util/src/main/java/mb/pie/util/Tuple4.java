package mb.pie.util;

public class Tuple4<T1, T2, T3, T4> {
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
}