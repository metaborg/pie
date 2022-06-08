package mb.pie.lang.test.funcDef.params.nullary;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class nullaryTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggernullaryComponent.class, -1);
    }
}
