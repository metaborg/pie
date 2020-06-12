package mb.pie.lang.test.binary.eq.eqBoolDifferent;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class EqBoolDifferentTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggereqBoolDifferentComponent.class, new Boolean(false));
    }
}
