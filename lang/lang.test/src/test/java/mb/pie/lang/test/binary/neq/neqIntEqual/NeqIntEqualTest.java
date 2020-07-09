package mb.pie.lang.test.binary.neq.neqIntEqual;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class NeqIntEqualTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerneqIntEqualComponent.class, new Boolean(false));
    }
}
