package mb.pie.lang.test.string;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class variableInterpolationVariableUsedTwiceOtherBetweenTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(new TaskDefsModule_variableInterpolationVariableUsedTwiceOtherBetweenTestGen(), main_variableInterpolationVariableUsedTwiceOtherBetween.class, "1234543212345");
    }
}
