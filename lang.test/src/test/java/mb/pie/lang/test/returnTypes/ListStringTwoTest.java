package mb.pie.lang.test.returnTypes;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ListStringTwoTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(new TaskDefsModule_listStringTwo(), main_listStringTwo.class, new ArrayList<>(Arrays.asList("hello", "world")));
    }
}
