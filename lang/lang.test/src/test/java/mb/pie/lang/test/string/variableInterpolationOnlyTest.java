package mb.pie.lang.test.string;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import mb.pie.api.Session;
import mb.pie.lang.test.util.PieRunner;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class variableInterpolationOnlyTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(new TaskDefsModule_variableInterpolationOnlyTestGen(), main_variableInterpolationOnly.class, "homeowner");
    }
}
