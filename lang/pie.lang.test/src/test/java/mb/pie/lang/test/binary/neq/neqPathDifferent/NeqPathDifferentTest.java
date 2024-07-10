package mb.pie.lang.test.binary.neq.neqPathDifferent;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class NeqPathDifferentTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerneqPathDifferentComponent.class, new Boolean(true));
    }
}
