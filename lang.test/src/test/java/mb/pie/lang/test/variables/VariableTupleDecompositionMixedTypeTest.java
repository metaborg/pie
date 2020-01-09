package mb.pie.lang.test.variables;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import mb.pie.api.PieSession;
import mb.pie.lang.test.util.PieRunner;
import mb.pie.util.Tuple2;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VariableTupleDecompositionMixedTypeTest {
    @Test void test() throws ExecException {
        final main_variableTupleDecompositionMixedType main = new main_variableTupleDecompositionMixedType();
        final PieRunner pieRunner = new PieRunner(main);
        try(PieSession session = pieRunner.newSession()) {
            final Tuple2<Boolean, String> output = session.require(main.createTask(None.instance));
            assertEquals(new Tuple2<>(new Boolean(true), "implicitly typed string"), output);
        }
    }
}
