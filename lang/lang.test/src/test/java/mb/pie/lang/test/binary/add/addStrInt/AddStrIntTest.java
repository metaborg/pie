package mb.pie.lang.test.binary.add.addStrInt;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class AddStrIntTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggeraddStrIntComponent.class, "String + int: 67");
    }
}
