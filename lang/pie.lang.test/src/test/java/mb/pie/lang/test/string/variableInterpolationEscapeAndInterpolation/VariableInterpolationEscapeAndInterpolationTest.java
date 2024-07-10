package mb.pie.lang.test.string.variableInterpolationEscapeAndInterpolation;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class VariableInterpolationEscapeAndInterpolationTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggervariableInterpolationEscapeAndInterpolationComponent.class, "$foo");
    }
}
