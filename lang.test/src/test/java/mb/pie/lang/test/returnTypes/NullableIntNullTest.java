package mb.pie.lang.test.returnTypes;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class NullableIntNullTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(new TaskDefsModule_nullableIntNull(), main_nullableIntNull.class, null);
    }
}
