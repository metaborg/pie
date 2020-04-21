package mb.pie.lang.test.returnTypes;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class NullableStringNullTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(new TaskDefsModule_nullableStringNullTestGen(), main_nullableStringNull.class, null);
    }
}
