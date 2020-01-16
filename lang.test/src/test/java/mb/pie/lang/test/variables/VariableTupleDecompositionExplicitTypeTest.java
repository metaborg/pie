package mb.pie.lang.test.variables;

import mb.pie.api.ExecException;
import mb.pie.util.Tuple2;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskoutputEquals;

class VariableTupleDecompositionExplicitTypeTest {
    @Test void test() throws ExecException {
        assertTaskoutputEquals(new TaskDefsModule_variableTupleDecompositionExplicitType(), main_variableTupleDecompositionExplicitType.class, new Tuple2<>("out of ideas for string values", new Integer(-11)));
    }
}
