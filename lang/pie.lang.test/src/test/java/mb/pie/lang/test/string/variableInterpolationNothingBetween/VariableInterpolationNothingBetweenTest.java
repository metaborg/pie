package mb.pie.lang.test.string.variableInterpolationNothingBetween;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class VariableInterpolationNothingBetweenTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggervariableInterpolationNothingBetweenComponent.class, "Oh no, it is the horrible snakeweasel!");
    }
}
