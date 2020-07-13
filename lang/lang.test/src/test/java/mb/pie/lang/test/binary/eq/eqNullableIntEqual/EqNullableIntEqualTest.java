package mb.pie.lang.test.binary.eq.eqNullableIntEqual;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class EqNullableIntEqualTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggereqNullableIntEqualComponent.class, new Boolean(true));
    }
}
