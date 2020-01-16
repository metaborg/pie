package mb.pie.lang.test.returnTypes;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskoutputEquals;

class NullableStringValueTest {
    @Test void test() throws ExecException {
        assertTaskoutputEquals(new TaskDefsModule_nullableStringValue(), main_nullableStringValue.class, "not null");
    }
}
