package mb.pie.lang.test.call.contextParams.omitted;

import mb.pie.api.ExecException;
import mb.pie.lang.test.call.contextParams.omitted.DaggeromittedComponent;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class OmittedTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggeromittedComponent.class, -1);
    }
}
