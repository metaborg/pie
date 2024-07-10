package mb.pie.lang.test.variables;

import java.io.Serializable;
import java.util.Objects;

public class Box<T> implements Serializable {
    public T t;

    public Box(T t) {
        this.t = t;
    }

    public T getT() {
        return t;
    }

    @Override public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        Box<?> box = (Box<?>)o;
        return Objects.equals(t, box.t);
    }

    @Override
    public int hashCode() {
        return Objects.hash(t);
    }

    @Override public String toString() {
        return "Box{" +
            "t=" + t +
            '}';
    }
}
