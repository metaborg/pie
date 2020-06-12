package mb.pie.lang.test.returnTypes.boolTrue;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class BoolTrueTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggerboolTrueComponent.class, new Boolean(true));
    }
}
