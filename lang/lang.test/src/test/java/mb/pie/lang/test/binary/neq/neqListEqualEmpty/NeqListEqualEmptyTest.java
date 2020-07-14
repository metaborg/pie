package mb.pie.lang.test.binary.neq.neqListEqualEmpty;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class NeqListEqualEmptyTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerneqListEqualEmptyComponent.class, new Boolean(false));
    }
}
