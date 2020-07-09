package mb.pie.lang.test.returnTypes.nullableStringNull;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class NullableStringNullTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggernullableStringNullComponent.class, null);
    }
}
