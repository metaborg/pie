package mb.pie.lang.test.unary;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskoutputEquals;

class NotTrueTest {
    @Test void test() throws ExecException {
        assertTaskoutputEquals(new TaskDefsModule_notTrue(), main_notTrue.class, new Boolean(false));
    }
}
