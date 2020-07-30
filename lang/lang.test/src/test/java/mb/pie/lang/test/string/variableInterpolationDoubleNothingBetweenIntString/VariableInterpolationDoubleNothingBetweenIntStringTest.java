package mb.pie.lang.test.string.variableInterpolationDoubleNothingBetweenIntString;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class VariableInterpolationDoubleNothingBetweenIntStringTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggervariableInterpolationDoubleNothingBetweenIntStringComponent.class, "42.71828");
    }
}
