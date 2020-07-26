package mb.pie.lang.test.binary.neq.neqTupleEqual;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class NeqTupleEqualTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerneqTupleEqualComponent.class, new Boolean(false));
    }
}
