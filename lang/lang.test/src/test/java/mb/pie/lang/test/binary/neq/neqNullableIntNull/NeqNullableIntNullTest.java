package mb.pie.lang.test.binary.neq.neqNullableIntNull;

import mb.pie.api.ExecException;
import mb.pie.lang.test.binary.eq.eqNullableIntNull.DaggereqNullableIntNullComponent;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class NeqNullableIntNullTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerneqNullableIntNullComponent.class, new Boolean(true));
    }
}
