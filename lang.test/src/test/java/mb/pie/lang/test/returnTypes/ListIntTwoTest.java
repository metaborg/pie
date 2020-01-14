package mb.pie.lang.test.returnTypes;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import mb.pie.api.PieSession;
import mb.pie.lang.test.util.PieRunner;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;

class ListIntTwoTest {
    @Test void test() throws ExecException {
        final main_listIntTwo main = new main_listIntTwo();
        final PieRunner pieRunner = new PieRunner(main);
        try(PieSession session = pieRunner.newSession()) {
            final ArrayList<Integer> output = session.require(main.createTask(None.instance));
            assertEquals(new ArrayList<>(Arrays.asList(1, 2)), output);
        }
    }
}