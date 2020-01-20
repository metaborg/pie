package mb.pie.lang.test.binary.eq;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class eqBoolDifferentTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(new TaskDefsModule_eqBoolDifferentTestGen(), main_eqBoolDifferent.class, new Boolean(false));
    }
}
