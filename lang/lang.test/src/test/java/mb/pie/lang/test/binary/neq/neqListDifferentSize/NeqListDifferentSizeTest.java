package mb.pie.lang.test.binary.neq.neqListDifferentSize;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class NeqListDifferentSizeTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerneqListDifferentSizeComponent.class, new Boolean(true));
    }
}
