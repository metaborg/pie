package mb.pie.lang.test.string.expressionInterpolationOnly;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ExpressionInterpolationOnlyTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerexpressionInterpolationOnlyComponent.class, "42");
    }
}
