package mb.pie.lang.test.variables.variableExplicitType;

import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class VariableExplicitTypeTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggervariableExplicitTypeComponent.class, "Greg ate a fish");
    }
}
