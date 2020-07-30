package mb.pie.lang.test.string.variableInterpolation;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class VariableInterpolationTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggervariableInterpolationComponent.class, "hello world!");
    }
}
