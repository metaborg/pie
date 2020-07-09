package mb.pie.lang.test.binary.add.addStrBool;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class AddStrBoolTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggeraddStrBoolComponent.class, "String + bool: true");
    }
}
