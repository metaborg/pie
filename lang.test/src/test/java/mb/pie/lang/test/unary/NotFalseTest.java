package mb.pie.lang.test.unary;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class NotFalseTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(new TaskDefsModule_notFalse(), main_notFalse.class, new Boolean(true));
    }
}
