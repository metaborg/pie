package mb.pie.lang.test.string.escapeDouble;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class EscapeDoubleTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerescapeDoubleComponent.class, "$$");
    }
}
