package mb.pie.lang.test.returnTypes.nullableStringValue;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class NullableStringValueTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggernullableStringValueComponent.class, "not null");
    }
}
