package mb.pie.lang.test.funcDef.twoFuncUnused;

import mb.pie.api.None;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class TwoFuncUnusedTest {
    @Test void test_main() throws Exception {
        assertTaskOutputEquals(DaggertwoFuncUnusedComponent.class, None.instance);
    }

    @Test void test_helper() throws Exception {
        assertTaskOutputEquals(DaggertwoFuncUnusedComponent.class, None.instance);
    }
}
