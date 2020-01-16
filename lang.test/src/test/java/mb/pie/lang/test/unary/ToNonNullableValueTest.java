package mb.pie.lang.test.unary;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskoutputEquals;

class ToNonNullableValueTest {
    @Test void test() throws ExecException {
        assertTaskoutputEquals(new TaskDefsModule_toNonNullableValue(), main_toNonNullableValue.class, "test string");
    }
}
