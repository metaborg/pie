package mb.pie.lang.test.returnTypes;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskoutputEquals;

class StringTest {
    @Test void test() throws ExecException {
        assertTaskoutputEquals(new TaskDefsModule_string(), main_string.class, "Hello, world!");
    }
}
