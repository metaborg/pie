package mb.pie.lang.test.unary;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ToNullableTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(new TaskDefsModule_toNullableTestGen(), main_toNullable.class, new ArrayList<Integer>(Arrays.asList(6)));
    }
}
