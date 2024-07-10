package mb.pie.lang.test.variables.anonymous.singlePassThrough;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class singlePassThroughTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggersinglePassThroughComponent.class, "hello world");
    }
}
