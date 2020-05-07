package mb.pie.lang.test.string;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class variableInterpolationVariableUsedTwiceTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(new TaskDefsModule_variableInterpolationVariableUsedTwiceTestGen(), main_variableInterpolationVariableUsedTwice.class, "Banana");
    }
}
