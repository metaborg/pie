package mb.pie.lang.test.variables.anonymous.multiDiscardAll;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class multiDiscardAllTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggermultiDiscardAllComponent.class, 78);
    }
}
