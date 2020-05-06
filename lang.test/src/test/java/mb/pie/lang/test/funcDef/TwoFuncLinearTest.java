package mb.pie.lang.test.funcDef;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class TwoFuncLinearTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(new TaskDefsModule_twoFuncLinearTestGen(), main_twoFuncLinear.class, None.instance);
    }
}
