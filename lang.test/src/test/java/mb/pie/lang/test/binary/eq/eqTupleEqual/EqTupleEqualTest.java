package mb.pie.lang.test.binary.eq.eqTupleEqual;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class EqTupleEqualTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggereqTupleEqualComponent.class, new Boolean(true));
    }
}
