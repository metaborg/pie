package mb.pie.lang.test.binary.add.addStrUnit;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class AddStrUnitTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggeraddStrUnitComponent.class, "String + unit: None()");
    }
}
