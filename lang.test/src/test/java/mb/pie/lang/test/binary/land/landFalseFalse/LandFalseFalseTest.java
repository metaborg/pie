package mb.pie.lang.test.binary.land.landFalseFalse;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class LandFalseFalseTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerlandFalseFalseComponent.class, new Boolean(false));
    }
}
