package mb.pie.lang.test.returnTypes;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskoutputEquals;

class BoolFalseTest {
    @Test void test() throws ExecException {
        assertTaskoutputEquals(new TaskDefsModule_boolFalse(), main_boolFalse.class, new Boolean(false));
    }
}
