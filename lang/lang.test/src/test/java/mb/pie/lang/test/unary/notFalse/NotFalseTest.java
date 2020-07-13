package mb.pie.lang.test.unary.notFalse;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class NotFalseTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggernotFalseComponent.class, new Boolean(true));
    }
}
