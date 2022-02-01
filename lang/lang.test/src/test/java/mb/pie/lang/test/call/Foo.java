package mb.pie.lang.test.call;

import java.io.Serializable;

public class Foo implements Serializable {
    public Foo() {}
    public Foo(int x) {}
    public Foo(boolean flag, String message) {}
    public Foo(String text, int x, int y) {}

    public static int func() {
        return 0;
    }
    public static int func(int x) {
        return x;
    }
    public static boolean func(boolean flag, String message) {
        return flag;
    }
    public static int func(String text, int x, int y) {
        return x+y;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return getClass().equals(obj.getClass());
    }
}
