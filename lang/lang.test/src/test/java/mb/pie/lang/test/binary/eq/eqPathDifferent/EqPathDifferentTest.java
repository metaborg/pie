package mb.pie.lang.test.binary.eq.eqPathDifferent;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class EqPathDifferentTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggereqPathDifferentComponent.class, new Boolean(false));
    }
}
