package mb.pie.lang.test.variables;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskoutputEquals;

class VariableImplicitTypeTest {
    @Test void test() throws ExecException {
        assertTaskoutputEquals(new TaskDefsModule_variableImplicitType(), main_variableImplicitType.class, new Integer(8));
    }
}
