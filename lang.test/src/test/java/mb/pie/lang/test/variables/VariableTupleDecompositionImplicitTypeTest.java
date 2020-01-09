package mb.pie.lang.test.variables;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import mb.pie.api.PieSession;
import mb.pie.lang.test.util.PieRunner;
import mb.pie.util.Tuple2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VariableTupleDecompositionImplicitTypeTest {
    @Test void test() throws ExecException {
        final main_variableTupleDecompositionImplicitType main = new main_variableTupleDecompositionImplicitType();
        final PieRunner pieRunner = new PieRunner(main);
        try(PieSession session = pieRunner.newSession()) {
            final Tuple2<String, Boolean> output = session.require(main.createTask(None.instance));
            assertEquals(new Tuple2<>("swapped values", new Boolean(true)), output);
        }
    }
}
