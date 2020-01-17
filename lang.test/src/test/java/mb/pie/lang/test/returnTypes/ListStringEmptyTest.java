package mb.pie.lang.test.returnTypes;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ListStringEmptyTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(new TaskDefsModule_listStringEmptyTestGen(), main_listStringEmpty.class, new ArrayList<>());
    }
}
