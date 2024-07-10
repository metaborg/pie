package mb.pie.lang.test.funcDef.params.two;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class twoTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(
            DaggertwoComponent.class,
            new main_two.Input(8, null),
            true
        );
    }
}
