package mb.pie.lang.test.returnTypes;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class IntTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(new TaskDefsModule_intTestGen(), main_int.class, new Integer(6));
    }
}
