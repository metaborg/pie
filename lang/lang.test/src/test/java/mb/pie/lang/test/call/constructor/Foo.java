package mb.pie.lang.test.call.constructor;

import java.io.Serializable;

public class Foo implements Serializable {
    public Foo() {}
    public Foo(int x) {}
    public Foo(boolean flag, String message) {}
    public Foo(String text, int x, int y) {}

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return getClass().equals(obj.getClass());
    }
}
