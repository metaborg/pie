package mb.pie.lang.test.returnTypes;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskoutputEquals;

class ListIntEmptyTest {
    @Test void test() throws ExecException {
        assertTaskoutputEquals(new TaskDefsModule_listIntEmpty(), main_listIntEmpty.class, new ArrayList<>());
    }
}
