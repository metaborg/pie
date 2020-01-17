package mb.pie.lang.test.unary;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ToNullableVarTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(new TaskDefsModule_toNullableVarTestGen(), main_toNullableVar.class, new Integer(6));
    }
}
