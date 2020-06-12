package mb.pie.lang.test.binary.eq.eqIntEqual;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class EqIntEqualTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggereqIntEqualComponent.class, new Boolean(true));
    }
}
