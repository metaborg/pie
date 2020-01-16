package mb.pie.lang.test.funcDef;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskoutputEquals;

class TwoFuncRecursiveTest {
    @Test @Timeout(5) void test() throws ExecException {
        assertTaskoutputEquals(new TaskDefsModule_twoFuncRecursive(), main_twoFuncRecursive.class, None.instance);
    }
}
