package mb.pie.lang.test.string.escapeSingle;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class EscapeSingleTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerescapeSingleComponent.class, "$");
    }
}
