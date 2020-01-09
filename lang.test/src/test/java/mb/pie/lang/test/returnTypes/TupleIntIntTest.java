package mb.pie.lang.test.returnTypes;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import mb.pie.api.PieSession;
import mb.pie.lang.test.util.PieRunner;
import mb.pie.util.Tuple2;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TupleIntIntTest {
    @Test void test() throws ExecException {
        final main_tupleIntInt main = new main_tupleIntInt();
        final PieRunner pieRunner = new PieRunner(main);
        try(PieSession session = pieRunner.newSession()) {
            final Tuple2<Integer, Integer> output = session.require(main.createTask(None.instance));
            assertEquals(new Tuple2(4, -90), output);
        }
    }
}
