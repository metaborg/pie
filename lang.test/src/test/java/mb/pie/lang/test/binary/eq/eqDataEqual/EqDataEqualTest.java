package mb.pie.lang.test.binary.eq.eqDataEqual;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class EqDataEqualTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggereqDataEqualComponent.class, new Boolean(true));
    }
}
