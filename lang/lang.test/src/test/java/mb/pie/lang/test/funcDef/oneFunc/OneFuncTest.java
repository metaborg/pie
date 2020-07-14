package mb.pie.lang.test.funcDef.oneFunc;

import mb.pie.api.None;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class OneFuncTest {
    @Test
    void test() throws Exception {
        assertTaskOutputEquals(DaggeroneFuncComponent.class, None.instance);
    }
}
