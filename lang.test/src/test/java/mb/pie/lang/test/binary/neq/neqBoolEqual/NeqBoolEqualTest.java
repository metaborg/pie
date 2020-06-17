package mb.pie.lang.test.binary.neq.neqBoolEqual;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class NeqBoolEqualTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerneqBoolEqualComponent.class, new Boolean(false));
    }
}
