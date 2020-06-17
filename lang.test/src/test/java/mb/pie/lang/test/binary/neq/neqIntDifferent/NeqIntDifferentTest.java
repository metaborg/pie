package mb.pie.lang.test.binary.neq.neqIntDifferent;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class NeqIntDifferentTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerneqIntDifferentComponent.class, new Boolean(true));
    }
}
