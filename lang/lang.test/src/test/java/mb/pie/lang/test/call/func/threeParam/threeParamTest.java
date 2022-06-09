package mb.pie.lang.test.call.func.threeParam;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class threeParamTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggerthreeParamComponent.class, 3);
    }
}
