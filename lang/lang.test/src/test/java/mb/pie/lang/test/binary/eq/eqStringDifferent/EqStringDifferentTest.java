package mb.pie.lang.test.binary.eq.eqStringDifferent;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class EqStringDifferentTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggereqStringDifferentComponent.class, new Boolean(false));
    }
}
