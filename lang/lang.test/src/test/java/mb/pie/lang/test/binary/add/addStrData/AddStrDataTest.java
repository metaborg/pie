package mb.pie.lang.test.binary.add.addStrData;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class AddStrDataTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggeraddStrDataComponent.class, "String + Sign (foreign data type): Sign with value 7");
    }
}
