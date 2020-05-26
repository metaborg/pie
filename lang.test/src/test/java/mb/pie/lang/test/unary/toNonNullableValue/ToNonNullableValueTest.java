package mb.pie.lang.test.unary.toNonNullableValue;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ToNonNullableValueTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggertoNonNullableValueComponent.class, "test string");
    }
}
