package mb.pie.lang.test.variables.variableTupleDecompositionImplicitType;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class VariableTupleDecompositionImplicitTypeTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggervariableTupleDecompositionImplicitTypeComponent.class, new main_variableTupleDecompositionImplicitType.Output("swapped values", new Boolean(true)));
    }
}
