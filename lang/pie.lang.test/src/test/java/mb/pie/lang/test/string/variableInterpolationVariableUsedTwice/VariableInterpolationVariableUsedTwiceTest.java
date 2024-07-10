package mb.pie.lang.test.string.variableInterpolationVariableUsedTwice;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class VariableInterpolationVariableUsedTwiceTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggervariableInterpolationVariableUsedTwiceComponent.class, "Banana");
    }
}
