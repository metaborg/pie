package mb.pie.lang.test.returnTypes;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskoutputEquals;

class UnitNoValueTest {
    @Test void test() throws ExecException {
        assertTaskoutputEquals(new TaskDefsModule_unitNoValue(), main_unitNoValue.class, None.instance);
    }
}
