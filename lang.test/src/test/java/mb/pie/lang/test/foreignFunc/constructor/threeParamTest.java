package mb.pie.lang.test.foreignFunc.constructor;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import mb.pie.api.Session;
import mb.pie.lang.test.util.PieRunner;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class threeParamTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(new TaskDefsModule_threeParamTestGen(), main_threeParam.class, new Foo());
    }
}
