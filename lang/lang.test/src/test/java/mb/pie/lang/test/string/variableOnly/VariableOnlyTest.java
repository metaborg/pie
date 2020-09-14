package mb.pie.lang.test.string.variableOnly;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class VariableOnlyTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggervariableOnlyComponent.class, "abc");
    }
}
