package mb.pie.lang.test.funcDef;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import mb.pie.api.PieSession;
import mb.pie.lang.test.util.PieRunner;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TwoFuncUnusedTest {
    @Test void test_main() throws ExecException {
        final main_twoFuncUnused main = new main_twoFuncUnused();
        final PieRunner pieRunner = new PieRunner(main);
        try(PieSession session = pieRunner.newSession()) {
            final None output = session.require(main.createTask(None.instance));
            assertEquals(None.instance, output);
        }
    }

    @Test void test_helper() throws ExecException {
        final helper_twoFuncUnused main = new helper_twoFuncUnused();
        final PieRunner pieRunner = new PieRunner(main);
        try(PieSession session = pieRunner.newSession()) {
            final None output = session.require(main.createTask(None.instance));
            assertEquals(None.instance, output);
        }
    }
}
