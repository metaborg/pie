package mb.pie.lang.test.string.expressionInterpolationTuple;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ExpressionInterpolationTupleTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerexpressionInterpolationTupleComponent.class, "(1, 2, 3)");
    }
}
