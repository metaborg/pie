package mb.pie.lang.test.variables.variableTupleAssignment;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class VariableTupleAssignmentTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggervariableTupleAssignmentComponent.class, new main_variableTupleAssignment.Output(new Integer(2), new ArrayList<>(Arrays.asList(new Boolean(true), new Boolean(false)))));
    }
}
