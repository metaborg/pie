package mb.pie.lang.test.variables.anonymous.singleInParamAndBody;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class singleInParamAndBodyTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggersingleInParamAndBodyComponent.class, "none", 26);
    }
}
