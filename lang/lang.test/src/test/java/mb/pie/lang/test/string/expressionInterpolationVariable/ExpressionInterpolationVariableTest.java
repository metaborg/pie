package mb.pie.lang.test.string.expressionInterpolationVariable;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ExpressionInterpolationVariableTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerexpressionInterpolationVariableComponent.class, "Hello Bob, destroyer of worlds.");
    }
}
