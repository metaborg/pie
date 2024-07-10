package mb.pie.lang.test.variables.variableGenericTypeSpecific;

import mb.pie.lang.test.variables.Box;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class VariableGenericTypeSpecificTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggervariableGenericTypeSpecificComponent.class, new Box<>(12), new Box<>(12));
    }
}
