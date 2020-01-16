package mb.pie.lang.test.variables;

import mb.pie.api.ExecException;
import mb.pie.util.Tuple2;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskoutputEquals;

class VariableTupleDecompositionMixedTypeTest {
    @Test void test() throws ExecException {
        assertTaskoutputEquals(new TaskDefsModule_variableTupleDecompositionMixedType(), main_variableTupleDecompositionMixedType.class, new Tuple2<>(new Boolean(true), "implicitly typed string"));
    }
}
