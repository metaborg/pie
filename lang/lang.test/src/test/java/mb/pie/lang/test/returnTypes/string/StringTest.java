package mb.pie.lang.test.returnTypes.string;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class StringTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggerstringComponent.class, "Hello, world!");
    }
}
