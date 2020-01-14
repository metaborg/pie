package mb.pie.lang.test.funcDef;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import mb.pie.api.PieSession;
import mb.pie.lang.test.util.PieRunner;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OneFuncTest {
    @Test void test() throws ExecException {
        final main_oneFunc main = new main_oneFunc();
        final PieRunner pieRunner = new PieRunner(main);
        try(PieSession session = pieRunner.newSession()) {
            final None output = session.require(main.createTask(None.instance));
            assertEquals(None.instance, output);
        }
    }
}
