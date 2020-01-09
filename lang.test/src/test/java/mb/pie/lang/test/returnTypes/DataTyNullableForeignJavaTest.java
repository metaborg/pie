package mb.pie.lang.test.returnTypes;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import mb.pie.api.PieSession;
import mb.pie.lang.test.util.PieRunner;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;

class DataTyNullableForeignJavaTest {
    @Test void test() throws ExecException {
        final main_dataTyNullableForeignJava main = new main_dataTyNullableForeignJava();
        final PieRunner pieRunner = new PieRunner(main);
        try(PieSession session = pieRunner.newSession()) {
            final Foo output = session.require(main.createTask(None.instance));
            assertEquals(null, output);
        }
    }
}
