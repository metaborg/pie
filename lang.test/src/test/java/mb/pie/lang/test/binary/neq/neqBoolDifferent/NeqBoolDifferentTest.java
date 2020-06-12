package mb.pie.lang.test.binary.neq.neqBoolDifferent;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class NeqBoolDifferentTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerneqBoolDifferentComponent.class, new Boolean(true));
    }
}
