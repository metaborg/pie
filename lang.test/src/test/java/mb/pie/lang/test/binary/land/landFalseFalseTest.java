package mb.pie.lang.test.binary.land;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import mb.pie.api.Session;
import mb.pie.lang.test.util.PieRunner;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class landFalseFalseTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(new TaskDefsModule_landFalseFalseTestGen(), main_landFalseFalse.class, new Boolean(false));
    }
}
