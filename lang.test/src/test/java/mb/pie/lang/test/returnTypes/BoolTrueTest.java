package mb.pie.lang.test.returnTypes;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class BoolTrueTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(new TaskDefsModule_boolTrueTestGen(), main_boolTrue.class, new Boolean(true));
    }
}
