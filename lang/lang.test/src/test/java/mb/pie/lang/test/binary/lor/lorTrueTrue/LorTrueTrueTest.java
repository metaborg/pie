package mb.pie.lang.test.binary.lor.lorTrueTrue;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class LorTrueTrueTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerlorTrueTrueComponent.class, new Boolean(true));
    }
}
