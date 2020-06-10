package mb.pie.lang.test.binary.neq.neqListDifferentElements;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class NeqListDifferentElementsTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerneqListDifferentElementsComponent.class, new Boolean(true));
    }
}
