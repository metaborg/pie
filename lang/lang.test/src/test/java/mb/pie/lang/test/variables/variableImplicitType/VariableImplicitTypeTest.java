package mb.pie.lang.test.variables.variableImplicitType;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class VariableImplicitTypeTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggervariableImplicitTypeComponent.class, new Integer(8));
    }
}
