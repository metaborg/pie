package mb.pie.lang.test.binary.neq.neqListEqual;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class NeqListEqualTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerneqListEqualComponent.class, new Boolean(false));
    }
}
