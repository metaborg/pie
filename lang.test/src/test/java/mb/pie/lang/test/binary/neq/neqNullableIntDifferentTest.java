package mb.pie.lang.test.binary.neq;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class neqNullableIntDifferentTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(new TaskDefsModule_neqNullableIntDifferentTestGen(), main_neqNullableIntDifferent.class, new Boolean(true));
    }
}
