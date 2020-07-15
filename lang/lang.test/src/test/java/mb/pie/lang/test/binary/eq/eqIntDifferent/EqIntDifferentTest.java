package mb.pie.lang.test.binary.eq.eqIntDifferent;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class EqIntDifferentTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggereqIntDifferentComponent.class, new Boolean(false));
    }
}
