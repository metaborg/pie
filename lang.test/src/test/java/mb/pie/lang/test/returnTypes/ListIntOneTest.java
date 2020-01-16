package mb.pie.lang.test.returnTypes;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskoutputEquals;

class ListIntOneTest {
    @Test void test() throws ExecException {
        assertTaskoutputEquals(new TaskDefsModule_listIntOne(), main_listIntOne.class, new ArrayList<>(Arrays.asList(46)));
    }
}
