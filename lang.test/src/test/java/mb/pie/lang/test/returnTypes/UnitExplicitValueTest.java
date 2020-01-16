package mb.pie.lang.test.returnTypes;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskoutputEquals;

class UnitExplicitValueTest {
    @Test void test() throws ExecException {
        assertTaskoutputEquals(new TaskDefsModule_unitExplicitValue(), main_unitExplicitValue.class, None.instance);
    }
}
