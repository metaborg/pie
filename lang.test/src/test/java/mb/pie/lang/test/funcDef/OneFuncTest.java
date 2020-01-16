package mb.pie.lang.test.funcDef;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskoutputEquals;

class OneFuncTest {
    @Test void test() throws ExecException {
        assertTaskoutputEquals(new TaskDefsModule_oneFunc(), main_oneFunc.class, None.instance);
    }
}
