package mb.pie.lang.test.variables;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import mb.pie.api.PieSession;
import mb.pie.lang.test.util.PieRunner;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VariableExplicitTypeTest {
    @Test void test() throws ExecException {
        final main_variableExplicitType main = new main_variableExplicitType();
        final PieRunner pieRunner = new PieRunner(main);
        try(PieSession session = pieRunner.newSession()) {
            final String output = session.require(main.createTask(None.instance));
            assertEquals("Greg ate a fish", output);
        }
    }
}
