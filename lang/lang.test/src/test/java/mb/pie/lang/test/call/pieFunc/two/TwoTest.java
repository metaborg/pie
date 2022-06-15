package mb.pie.lang.test.call.pieFunc.two;

import mb.pie.api.ExecException;
import mb.pie.lang.test.call.pieFunc.two.DaggertwoComponent;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class TwoTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggertwoComponent.class, 2);
    }
}
