package mb.pie.lang.test.binary.eq.eqStringEqual;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class EqStringEqualTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggereqStringEqualComponent.class, new Boolean(true));
    }
}
