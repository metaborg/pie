package mb.pie.lang.test.binary.neq.neqStringDifferent;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class NeqStringDifferentTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerneqStringDifferentComponent.class, new Boolean(true));
    }
}
