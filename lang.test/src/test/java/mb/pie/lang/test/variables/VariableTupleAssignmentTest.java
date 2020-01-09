package mb.pie.lang.test.variables;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import mb.pie.api.PieSession;
import mb.pie.lang.test.util.PieRunner;
import mb.pie.util.Tuple2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;

class VariableTupleAssignmentTest {
    @Test void test() throws ExecException {
        final main_variableTupleAssignment main = new main_variableTupleAssignment();
        final PieRunner pieRunner = new PieRunner(main);
        try(PieSession session = pieRunner.newSession()) {
            final Tuple2<Integer, ArrayList<Boolean>> output = session.require(main.createTask(None.instance));
            assertEquals(new Tuple2<>(new Integer(2), new ArrayList<>(Arrays.asList(new Boolean(true), new Boolean(false)))), output);
        }
    }
}
