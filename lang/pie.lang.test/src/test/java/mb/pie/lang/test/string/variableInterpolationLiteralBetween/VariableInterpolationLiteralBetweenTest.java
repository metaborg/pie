package mb.pie.lang.test.string.variableInterpolationLiteralBetween;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class VariableInterpolationLiteralBetweenTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggervariableInterpolationLiteralBetweenComponent.class, "Hello Bob, I am from somewhere over the rainbow.");
    }
}
