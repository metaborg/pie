package mb.pie.lang.test;

import mb.pie.api.PieSession;
import mb.pie.lang.test.util.PieRunner;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimplePieLangTest {
    @Test void test() {
        final PieRunner pieRunner = new PieRunner(/* TODO: add taskdef */);
        try(PieSession session = pieRunner.newSession()) {
//            final String output = session.require(/* TODO: run task */);
//            assertEquals(output, "Hello, world!");
        }
    }
}
