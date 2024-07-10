package mb.pie.lang.test.string.variableInterpolationOnly;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class VariableInterpolationOnlyTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggervariableInterpolationOnlyComponent.class, "homeowner");
    }
}
