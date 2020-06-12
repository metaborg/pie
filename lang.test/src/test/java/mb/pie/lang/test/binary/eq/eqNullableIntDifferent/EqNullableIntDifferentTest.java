package mb.pie.lang.test.binary.eq.eqNullableIntDifferent;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class EqNullableIntDifferentTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggereqNullableIntDifferentComponent.class, new Boolean(false));
    }
}
