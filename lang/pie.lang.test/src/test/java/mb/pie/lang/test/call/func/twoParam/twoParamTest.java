package mb.pie.lang.test.call.func.twoParam;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class twoParamTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggertwoParamComponent.class, 2);
    }
}
