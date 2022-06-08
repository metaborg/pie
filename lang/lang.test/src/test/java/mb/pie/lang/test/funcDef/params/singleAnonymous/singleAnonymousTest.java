package mb.pie.lang.test.funcDef.params.singleAnonymous;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class singleAnonymousTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggersingleAnonymousComponent.class, 3, 9);
    }
}
