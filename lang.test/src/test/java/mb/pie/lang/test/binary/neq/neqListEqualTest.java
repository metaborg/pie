package mb.pie.lang.test.binary.neq;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class neqListEqualTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(new TaskDefsModule_neqListEqualTestGen(), main_neqListEqual.class, new Boolean(false));
    }
}
