package mb.pie.lang.test.binary.add.addStrTuple;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class AddStrTupleTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggeraddStrTupleComponent.class, "String + Tuple: (1, true)");
    }
}
