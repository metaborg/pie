package mb.pie.lang.test.binary.lor.lorFalseFalse;

import mb.pie.api.ExecException;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class LorFalseFalseTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerlorFalseFalseComponent.class, new Boolean(false));
    }
}
