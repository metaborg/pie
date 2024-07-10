package mb.pie.lang.test.string.variableInterpolationDoubleNothingBetweenIntInt;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class VariableInterpolationDoubleNothingBetweenIntIntTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggervariableInterpolationDoubleNothingBetweenIntIntComponent.class, "1064");
    }
}
