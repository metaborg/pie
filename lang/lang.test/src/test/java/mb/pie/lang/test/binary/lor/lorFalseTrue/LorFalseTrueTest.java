package mb.pie.lang.test.binary.lor.lorFalseTrue;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class LorFalseTrueTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerlorFalseTrueComponent.class, new Boolean(true));
    }
}
