package mb.pie.lang.test.foreignFunc.constructor;

import java.io.Serializable;

public class Bar<T> implements Serializable {
    private T t;
    private String arg;

    public <E>Bar(T t, E e, String arg) {
        this.t = t;
        this.arg = arg;
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
