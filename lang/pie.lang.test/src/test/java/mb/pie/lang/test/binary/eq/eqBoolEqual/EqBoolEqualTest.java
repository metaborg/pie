package mb.pie.lang.test.binary.eq.eqBoolEqual;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class EqBoolEqualTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggereqBoolEqualComponent.class, new Boolean(true));
    }
}
