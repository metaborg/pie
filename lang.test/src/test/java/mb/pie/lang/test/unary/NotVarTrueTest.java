package mb.pie.lang.test.unary;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskoutputEquals;

class NotVarTrueTest {
    @Test void test() throws ExecException {
        assertTaskoutputEquals(new TaskDefsModule_notVarTrue(), main_notVarTrue.class, new Boolean(false));
    }
}
