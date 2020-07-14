package mb.pie.lang.test.variables.variableTupleDecompositionMixedType;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class VariableTupleDecompositionMixedTypeTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggervariableTupleDecompositionMixedTypeComponent.class, new main_variableTupleDecompositionMixedType.Output(new Boolean(true), "implicitly typed string"));
    }
}
