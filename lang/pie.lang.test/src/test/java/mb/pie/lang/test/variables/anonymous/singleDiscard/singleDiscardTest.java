package mb.pie.lang.test.variables.anonymous.singleDiscard;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class singleDiscardTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggersingleDiscardComponent.class, 5);
    }
}
