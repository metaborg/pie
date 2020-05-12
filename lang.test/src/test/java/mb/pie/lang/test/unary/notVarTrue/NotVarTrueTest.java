package mb.pie.lang.test.unary.notVarTrue;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class NotVarTrueTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggernotVarTrueComponent.class, new Boolean(false));
    }
}
