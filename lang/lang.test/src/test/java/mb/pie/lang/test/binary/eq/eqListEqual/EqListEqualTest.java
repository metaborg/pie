package mb.pie.lang.test.binary.eq.eqListEqual;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class EqListEqualTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggereqListEqualComponent.class, new Boolean(true));
    }
}
