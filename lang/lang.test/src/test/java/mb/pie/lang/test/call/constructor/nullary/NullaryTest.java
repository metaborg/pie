package mb.pie.lang.test.call.constructor.nullary;

import mb.pie.api.ExecException;
import mb.pie.lang.test.call.constructor.Foo;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class NullaryTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggernullaryComponent.class, new Foo());
    }
}
