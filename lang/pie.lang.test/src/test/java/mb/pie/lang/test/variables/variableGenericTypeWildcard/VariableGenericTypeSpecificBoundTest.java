package mb.pie.lang.test.variables.variableGenericTypeWildcard;

import mb.pie.lang.test.variables.Box;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class VariableGenericTypeSpecificBoundTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggervariableGenericTypeWildcardComponent.class, new Box<>("Wildcard box"), new Box<>("Wildcard box"));
    }
}
