package mb.pie.lang.test.call.contextParams.one;

import mb.pie.api.ExecException;
import mb.pie.lang.test.call.contextParams.one.DaggeroneComponent;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class OneTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggeroneComponent.class, 1);
    }
}
