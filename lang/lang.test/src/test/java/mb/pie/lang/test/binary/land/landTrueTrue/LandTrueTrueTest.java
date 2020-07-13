package mb.pie.lang.test.binary.land.landTrueTrue;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class LandTrueTrueTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerlandTrueTrueComponent.class, new Boolean(true));
    }
}
