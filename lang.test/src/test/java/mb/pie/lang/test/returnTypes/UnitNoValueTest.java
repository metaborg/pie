package mb.pie.lang.test.returnTypes;

import mb.pie.api.ExecException;
import mb.pie.api.None;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class UnitNoValueTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(new TaskDefsModule_unitNoValueTestGen(), main_unitNoValue.class, None.instance);
    }
}
