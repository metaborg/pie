package mb.pie.lang.test.unary;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskoutputEquals;

class ToNullableTest {
    @Test void test() throws ExecException {
        assertTaskoutputEquals(new TaskDefsModule_toNullable, main_toNullable.class, new ArrayList<>(Arrays.asList(6)));
    }
}
