package mb.pie.util;

public class Tuple2<T1, T2> {
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
}
