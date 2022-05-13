package mb.pie.lang.test.call.func.singleParam;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class singleParamTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggersingleParamComponent.class, 1);
    }
}
