package mb.pie.lang.test.string.escapeBackslash;

import mb.pie.api.ExecException;
import mb.pie.lang.test.string.escapeBackslash.DaggerescapeBackslashComponent;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class EscapeBackslashTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerescapeBackslashComponent.class, "\\valueOfFoo");
    }
}
