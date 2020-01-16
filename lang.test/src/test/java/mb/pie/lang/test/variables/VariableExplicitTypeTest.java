package mb.pie.lang.test.variables;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskoutputEquals;

class VariableExplicitTypeTest {
    @Test void test() throws ExecException {
        assertTaskoutputEquals(new TaskDefsModule_variableExplicitType(), main_variableExplicitType.class, "Greg ate a fish");
    }
}
