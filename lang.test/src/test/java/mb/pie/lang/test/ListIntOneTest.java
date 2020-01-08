package mb.pie.lang.test;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import mb.pie.api.PieSession;
import mb.pie.lang.test.util.PieRunner;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;

class ListIntOneTest {
    @Test void test() throws ExecException {
        final main_listIntOne main = new main_listIntOne();
        final PieRunner pieRunner = new PieRunner(main);
        try(PieSession session = pieRunner.newSession()) {
            final ArrayList<Integer> output = session.require(main.createTask(None.instance));
            assertEquals(new ArrayList<>(Arrays.asList(46)), output);
        }
    }
}
