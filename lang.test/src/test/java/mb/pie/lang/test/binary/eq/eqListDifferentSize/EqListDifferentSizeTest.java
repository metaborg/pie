package mb.pie.lang.test.binary.eq.eqListDifferentSize;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class EqListDifferentSizeTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggereqListDifferentSizeComponent.class, new Boolean(false));
    }
}
