package mb.pie.lang.test.binary.eq.eqListDifferentElements;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class EqListDifferentElementsTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggereqListDifferentElementsComponent.class, new Boolean(false));
    }
}
