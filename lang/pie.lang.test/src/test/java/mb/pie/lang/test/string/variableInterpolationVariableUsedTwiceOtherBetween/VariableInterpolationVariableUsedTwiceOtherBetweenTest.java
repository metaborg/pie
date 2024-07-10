package mb.pie.lang.test.string.variableInterpolationVariableUsedTwiceOtherBetween;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class VariableInterpolationVariableUsedTwiceOtherBetweenTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggervariableInterpolationVariableUsedTwiceOtherBetweenComponent.class, "1234543212345");
    }
}
