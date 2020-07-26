package mb.pie.lang.test.binary.neq.neqNullableIntDifferent;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class NeqNullableIntDifferentTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerneqNullableIntDifferentComponent.class, new Boolean(true));
    }
}
