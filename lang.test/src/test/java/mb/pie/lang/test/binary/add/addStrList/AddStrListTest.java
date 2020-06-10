package mb.pie.lang.test.binary.add.addStrList;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class AddStrListTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggeraddStrListComponent.class, "String + list: [1, 2, 3]");
    }
}
