package mb.pie.lang.test.binary.eq.eqPathEqual;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class EqPathEqualTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggereqPathEqualComponent.class, new Boolean(true));
    }
}
