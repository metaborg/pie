package mb.pie.lang.test.call.constructor.generic;

import mb.pie.api.ExecException;
import mb.pie.lang.test.call.Bar;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class GenericTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggergenericComponent.class, new Bar(58, false, "generic"));
    }
}
