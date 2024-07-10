package mb.pie.lang.test.string.expressionInterpolation;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ExpressionInterpolationTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerexpressionInterpolationComponent.class, "1 + 2 = 3.");
    }
}
