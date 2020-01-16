package mb.pie.lang.test.unary;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskoutputEquals;

class NotVarFalseTest {
    @Test void test() throws ExecException {
        assertTaskoutputEquals(new TaskDefsModule_notVarFalse(), main_notVarFalse.class, new Boolean(true));
    }
}
