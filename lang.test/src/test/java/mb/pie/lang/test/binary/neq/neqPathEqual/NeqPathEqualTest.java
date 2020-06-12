package mb.pie.lang.test.binary.neq.neqPathEqual;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class NeqPathEqualTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerneqPathEqualComponent.class, new Boolean(false));
    }
}
