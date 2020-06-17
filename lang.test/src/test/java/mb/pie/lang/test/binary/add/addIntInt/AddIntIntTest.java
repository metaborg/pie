package mb.pie.lang.test.binary.add.addIntInt;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class AddIntIntTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggeraddIntIntComponent.class, 9);
    }
}
