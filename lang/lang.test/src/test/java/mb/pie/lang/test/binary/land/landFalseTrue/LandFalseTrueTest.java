package mb.pie.lang.test.binary.land.landFalseTrue;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class LandFalseTrueTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerlandFalseTrueComponent.class, new Boolean(false));
    }
}
