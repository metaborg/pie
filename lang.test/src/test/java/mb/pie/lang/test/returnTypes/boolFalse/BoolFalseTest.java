package mb.pie.lang.test.returnTypes.boolFalse;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class BoolFalseTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggerboolFalseComponent.class, new Boolean(false));
    }
}
