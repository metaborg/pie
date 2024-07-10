package mb.pie.lang.test.variables.variableTupleDecompositionExplicitType;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class VariableTupleDecompositionExplicitTypeTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggervariableTupleDecompositionExplicitTypeComponent.class, new main_variableTupleDecompositionExplicitType.Output("out of ideas for string values", new Integer(-11)));
    }
}
