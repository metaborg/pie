package mb.pie.lang.test.funcDef.contextParams.omitted;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class OmittedTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggeromittedComponent.class, -1);
    }
}
