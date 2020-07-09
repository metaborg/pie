package mb.pie.lang.test.binary.neq.neqDataEqual;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class NeqDataEqualTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerneqDataEqualComponent.class, new Boolean(false));
    }
}
