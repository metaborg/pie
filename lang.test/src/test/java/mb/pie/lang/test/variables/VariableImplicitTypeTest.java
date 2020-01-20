package mb.pie.lang.test.variables;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class VariableImplicitTypeTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(new TaskDefsModule_variableImplicitTypeTestGen(), main_variableImplicitType.class, new Integer(8));
    }
}
