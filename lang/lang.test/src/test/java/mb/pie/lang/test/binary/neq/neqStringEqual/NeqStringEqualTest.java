package mb.pie.lang.test.binary.neq.neqStringEqual;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class NeqStringEqualTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerneqStringEqualComponent.class, new Boolean(false));
    }
}
