package mb.pie.lang.test.binary.neq.neqNullableIntEqual;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class NeqNullableIntEqualTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerneqNullableIntEqualComponent.class, new Boolean(false));
    }
}
