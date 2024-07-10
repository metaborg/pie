package mb.pie.lang.test.call.func.twoParamBothAnonymous;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class twoParamBothAnonymousTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggertwoParamBothAnonymousComponent.class, 20);
    }
}
