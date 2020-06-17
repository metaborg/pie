package mb.pie.lang.test.unary.toNullableVar;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ToNullableVarTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggertoNullableVarComponent.class, new Integer(6));
    }
}
