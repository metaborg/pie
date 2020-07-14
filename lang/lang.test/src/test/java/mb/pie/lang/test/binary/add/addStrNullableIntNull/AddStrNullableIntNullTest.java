package mb.pie.lang.test.binary.add.addStrNullableIntNull;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class AddStrNullableIntNullTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggeraddStrNullableIntNullComponent.class, "String + Nullable Int: null");
    }
}
