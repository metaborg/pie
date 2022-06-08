package mb.pie.lang.test.call.func.singleParamAnonymous;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class singleParamAnonymousTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggersingleParamAnonymousComponent.class, 2);
    }
}
