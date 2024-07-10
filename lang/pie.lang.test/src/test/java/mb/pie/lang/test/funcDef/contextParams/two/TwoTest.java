package mb.pie.lang.test.funcDef.contextParams.two;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class TwoTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggertwoComponent.class, 3);
    }
}
