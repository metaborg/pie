package mb.pie.lang.test.variables.anonymous.singleMultiplePassThrough;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class singleMultiplePassThroughTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggersingleMultiplePassThroughComponent.class, "hello 23");
    }
}
