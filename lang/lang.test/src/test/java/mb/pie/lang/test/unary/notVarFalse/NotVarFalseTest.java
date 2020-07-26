package mb.pie.lang.test.unary.notVarFalse;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class NotVarFalseTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggernotVarFalseComponent.class, new Boolean(true));
    }
}
