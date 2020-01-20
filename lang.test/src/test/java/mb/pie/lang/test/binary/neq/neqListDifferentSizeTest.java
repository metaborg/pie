package mb.pie.lang.test.binary.neq;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class neqListDifferentSizeTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(new TaskDefsModule_neqListDifferentSizeTestGen(), main_neqListDifferentSize.class, new Boolean(true));
    }
}
