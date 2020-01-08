package mb.pie.lang.test;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import mb.pie.api.PieSession;
import mb.pie.lang.test.util.PieRunner;
import mb.resource.fs.FSPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PathRelativeTest {
    @Test void test() throws ExecException {
        final main_pathRelative main = new main_pathRelative();
        final PieRunner pieRunner = new PieRunner(main);
        try(PieSession session = pieRunner.newSession()) {
            final FSPath output = session.require(main.createTask(None.instance));
            assertEquals(new FSPath("./path/to/foo"), output);
        }
    }
}
