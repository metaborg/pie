package mb.pie.lang.test.binary.eq.eqNullableIntNull;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class EqNullableIntNullTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggereqNullableIntNullComponent.class, new Boolean(false));
    }
}
