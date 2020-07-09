package mb.pie.lang.test.binary.add.addListElem;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class AddListElemTest {
    @Test void test() throws ExecException {
        ArrayList<Integer> expected = new ArrayList<>(Arrays.asList(1, 2, 3));
        assertTaskOutputEquals(DaggeraddListElemComponent.class, expected);
    }
}
