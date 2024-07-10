package mb.pie.lang.test.funcDef.contextParams.one;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class OneTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggeroneComponent.class, 1);
    }
}
