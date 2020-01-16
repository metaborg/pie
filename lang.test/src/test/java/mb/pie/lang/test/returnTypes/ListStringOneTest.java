package mb.pie.lang.test.returnTypes;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskoutputEquals;

class ListStringOneTest {
    @Test void test() throws ExecException {
        assertTaskoutputEquals(new TaskDefsModule_listStringOne(), main_listStringOne.class, new ArrayList<>(Arrays.asList("first")));
    }
}
