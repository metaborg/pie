package mb.pie.lang.test.binary.lor.lorTrueFalse;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class LorTrueFalseTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerlorTrueFalseComponent.class, new Boolean(true));
    }
}
