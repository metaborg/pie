package mb.pie.lang.test.returnTypes.nullableIntNull;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class NullableIntNullTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggernullableIntNullComponent.class, null);
    }
}
