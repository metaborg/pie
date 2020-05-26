package mb.pie.lang.test.binary.neq.neqTupleDifferent;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class NeqTupleDifferentTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerneqTupleDifferentComponent.class, new Boolean(true));
    }
}
