package mb.pie.lang.test.variables;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import mb.pie.api.PieSession;
import mb.pie.lang.test.util.PieRunner;
import mb.pie.util.Tuple2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VariableTupleDecompositionExplicitTypeTest {
    @Test void test() throws ExecException {
        final main_variableTupleDecompositionExplicitType main = new main_variableTupleDecompositionExplicitType();
        final PieRunner pieRunner = new PieRunner(main);
        try(PieSession session = pieRunner.newSession()) {
            final Tuple2<String, Integer> output = session.require(main.createTask(None.instance));
            assertEquals(new Tuple2<>("out of ideas for string values", new Integer(-11)), output);
        }
    }
}
