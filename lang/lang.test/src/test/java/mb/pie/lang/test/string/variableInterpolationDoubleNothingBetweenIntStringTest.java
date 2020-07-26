package mb.pie.lang.test.string;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class variableInterpolationDoubleNothingBetweenIntStringTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(new TaskDefsModule_variableInterpolationDoubleNothingBetweenIntStringTestGen(), main_variableInterpolationDoubleNothingBetweenIntString.class, "42.71828");
    }
}
