package mb.pie.lang.test.string.expressionInterpolationLiteralBetween;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ExpressionInterpolationLiteralBetweenTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerexpressionInterpolationLiteralBetweenComponent.class, "2 + 2 = 4 = 4 = 3 + 1");
    }
}
