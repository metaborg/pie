package mb.pie.lang.test.variables.anonymous.singleOutsideBlock;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class singleOutsideBlockTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggersingleOutsideBlockComponent.class, "look ma, no block");
    }
}
