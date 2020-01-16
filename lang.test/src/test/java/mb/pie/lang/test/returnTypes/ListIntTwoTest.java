package mb.pie.lang.test.returnTypes;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskoutputEquals;

class ListIntTwoTest {
    @Test void test() throws ExecException {
        assertTaskoutputEquals(new TaskDefsModule_listIntTwo(), main_listIntTwo.class, new ArrayList<>(Arrays.asList(1, 2)));
    }
}
