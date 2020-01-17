package mb.pie.lang.test.returnTypes;

import mb.pie.api.ExecException;
import mb.pie.util.Tuple2;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class TupleBoolStringTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(new TaskDefsModule_tupleBoolStringTestGen(), main_tupleBoolString.class, new main_tupleBoolString.Output(false, "hey"));
    }
}
