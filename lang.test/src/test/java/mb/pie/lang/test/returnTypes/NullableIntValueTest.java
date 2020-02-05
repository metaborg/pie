package mb.pie.lang.test.returnTypes;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class NullableIntValueTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(new TaskDefsModule_nullableIntValueTestGen(), main_nullableIntValue.class, new Integer(0));
    }
}
