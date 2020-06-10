package mb.pie.lang.test.unary.toNullable;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ToNullableTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggertoNullableComponent.class, new ArrayList<Integer>(Arrays.asList(6)));
    }
}
