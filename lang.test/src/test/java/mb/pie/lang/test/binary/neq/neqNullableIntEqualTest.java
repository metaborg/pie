package mb.pie.lang.test.binary.neq;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class neqNullableIntEqualTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(new TaskDefsModule_neqNullableIntEqualTestGen(), main_neqNullableIntEqual.class, new Boolean(false));
    }
}
