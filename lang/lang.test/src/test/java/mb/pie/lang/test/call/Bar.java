package mb.pie.lang.test.call;

import java.io.Serializable;

public class Bar<T> implements Serializable {
    private T t;
    private String arg;

    public <E>Bar(T t, E e, String arg) {
        this.t = t;
        this.arg = arg;
    }

    public static <C, D> D func(C c, D d) {
        return d;
    }

    public <C, D> T method(C c, D d) {
        return t;
    }

    @Override
    public int hashCode() {
        return t.hashCode() * 31 + arg.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!getClass().equals(obj.getClass())) {
            return false;
        };
        Bar that = (Bar) obj;
        return this.t.equals(that.t) && this.arg.equals(that.arg);
    }
}
