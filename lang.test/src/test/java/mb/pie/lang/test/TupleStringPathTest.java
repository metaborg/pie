package mb.pie.lang.test;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import mb.pie.api.PieSession;
import mb.pie.lang.test.util.PieRunner;
import mb.pie.util.Tuple2;
import mb.resource.fs.FSPath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TupleStringPathTest {
    @Test void test() throws ExecException {
        final main_tupleStringPath main = new main_tupleStringPath();
        final PieRunner pieRunner = new PieRunner(main);
        try(PieSession session = pieRunner.newSession()) {
            final Tuple2<String, FSPath> output = session.require(main.createTask(None.instance));
            assertEquals(new Tuple2("Folder with pictures", new FSPath("/c/home/bob/pictures")), output);
        }
    }
}
