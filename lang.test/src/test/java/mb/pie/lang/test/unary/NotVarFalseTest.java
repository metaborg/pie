package mb.pie.lang.test.unary;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class NotVarFalseTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(new TaskDefsModule_notVarFalse(), main_notVarFalse.class, new Boolean(true));
    }
}
