package mb.pie.lang.test.variables;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class VariableExplicitTypeTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(new TaskDefsModule_variableExplicitTypeTestGen(), main_variableExplicitType.class, "Greg ate a fish");
    }
}
