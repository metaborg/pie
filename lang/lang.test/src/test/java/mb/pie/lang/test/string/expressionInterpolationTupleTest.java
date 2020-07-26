package mb.pie.lang.test.string;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import mb.pie.api.Session;
import mb.pie.lang.test.util.PieRunner;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class expressionInterpolationTupleTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(new TaskDefsModule_expressionInterpolationTupleTestGen(), main_expressionInterpolationTuple.class, "(1, 2, 3)");
    }
}
