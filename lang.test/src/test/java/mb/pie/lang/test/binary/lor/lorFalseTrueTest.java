package mb.pie.lang.test.binary.lor;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import mb.pie.api.Session;
import mb.pie.lang.test.util.PieRunner;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class lorFalseTrueTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(new TaskDefsModule_lorFalseTrueTestGen(), main_lorFalseTrue.class, new Boolean(true));
    }
}
