package mb.pie.lang.test.binary.add.addStrNullableIntValue;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class AddStrNullableIntValueTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggeraddStrNullableIntValueComponent.class, "String + Nullable Int: 45");
    }
}
