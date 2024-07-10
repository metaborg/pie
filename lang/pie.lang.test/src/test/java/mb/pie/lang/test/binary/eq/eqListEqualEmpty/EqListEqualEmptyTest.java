package mb.pie.lang.test.binary.eq.eqListEqualEmpty;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class EqListEqualEmptyTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggereqListEqualEmptyComponent.class, new Boolean(true));
    }
}
