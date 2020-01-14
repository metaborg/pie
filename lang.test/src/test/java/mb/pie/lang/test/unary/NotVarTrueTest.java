package mb.pie.lang.test.unary;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import mb.pie.api.PieSession;
import mb.pie.lang.test.util.PieRunner;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotVarTrueTest {
    @Test void test() throws ExecException {
        final main_notVarTrue main = new main_notVarTrue();
        final PieRunner pieRunner = new PieRunner(main);
        try(PieSession session = pieRunner.newSession()) {
            final Boolean output = session.require(main.createTask(None.instance));
            assertEquals(new Boolean(false), output);
        }
    }
}