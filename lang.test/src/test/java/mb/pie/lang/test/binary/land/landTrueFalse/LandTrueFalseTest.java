package mb.pie.lang.test.binary.land.landTrueFalse;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class LandTrueFalseTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerlandTrueFalseComponent.class, new Boolean(false));
    }
}
