package mb.pie.lang.test.call.func.twoParamOneAnonymous;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class twoParamOneAnonymousTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggertwoParamOneAnonymousComponent.class, 21);
    }
}
