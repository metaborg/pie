package mb.pie.lang.test.binary.eq.eqDataDifferent;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class EqDataDifferentTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggereqDataDifferentComponent.class, new Boolean(false));
    }
}
