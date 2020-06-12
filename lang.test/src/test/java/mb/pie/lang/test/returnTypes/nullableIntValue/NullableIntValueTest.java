package mb.pie.lang.test.returnTypes.nullableIntValue;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class NullableIntValueTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggernullableIntValueComponent.class, new Integer(0));
    }
}
