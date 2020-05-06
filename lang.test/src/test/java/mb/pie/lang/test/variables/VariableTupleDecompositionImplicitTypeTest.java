package mb.pie.lang.test.variables;

import mb.pie.api.ExecException;
import mb.pie.util.Tuple2;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class VariableTupleDecompositionImplicitTypeTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(new TaskDefsModule_variableTupleDecompositionImplicitTypeTestGen(), main_variableTupleDecompositionImplicitType.class, new main_variableTupleDecompositionImplicitType.Output("swapped values", new Boolean(true)));
    }
}
