package mb.pie.lang.test.unary.notTrue;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class NotTrueTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggernotTrueComponent.class, new Boolean(false));
    }
}
