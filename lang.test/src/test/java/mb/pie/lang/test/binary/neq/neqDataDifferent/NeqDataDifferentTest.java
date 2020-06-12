package mb.pie.lang.test.binary.neq.neqDataDifferent;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class NeqDataDifferentTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerneqDataDifferentComponent.class, new Boolean(true));
    }
}
