package mb.pie.lang.test.string;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class variableInterpolationDoubleNothingBetweenIntIntTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(new TaskDefsModule_variableInterpolationDoubleNothingBetweenIntIntTestGen(), main_variableInterpolationDoubleNothingBetweenIntInt.class, "1064");
    }
}
