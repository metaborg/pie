package mb.pie.lang.test.call.func.nullary;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class nullaryTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggernullaryComponent.class, 0);
    }
}
