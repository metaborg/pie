package mb.pie.lang.test.string.literal;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class LiteralTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerliteralComponent.class, "a string");
    }
}
