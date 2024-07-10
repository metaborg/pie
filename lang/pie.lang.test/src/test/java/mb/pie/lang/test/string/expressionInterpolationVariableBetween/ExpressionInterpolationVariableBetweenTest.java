package mb.pie.lang.test.string.expressionInterpolationVariableBetween;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ExpressionInterpolationVariableBetweenTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerexpressionInterpolationVariableBetweenComponent.class, "27 = 27");
    }
}
