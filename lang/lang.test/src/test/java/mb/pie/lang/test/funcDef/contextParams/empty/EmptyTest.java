package mb.pie.lang.test.funcDef.contextParams.empty;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class EmptyTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggeremptyComponent.class, 0);
    }
}
