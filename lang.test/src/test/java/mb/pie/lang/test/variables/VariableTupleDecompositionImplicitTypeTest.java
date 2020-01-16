package mb.pie.lang.test.variables;

import mb.pie.api.ExecException;
import mb.pie.util.Tuple2;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskoutputEquals;

class VariableTupleDecompositionImplicitTypeTest {
    @Test void test() throws ExecException {
        assertTaskoutputEquals(new TaskDefsModule_variableTupleDecompositionImplicitType(), main_variableTupleDecompositionImplicitType.class, new Tuple2<>("swapped values", new Boolean(true)));
    }
}
