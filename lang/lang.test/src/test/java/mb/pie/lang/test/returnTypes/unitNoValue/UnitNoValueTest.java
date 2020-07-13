package mb.pie.lang.test.returnTypes.unitNoValue;

import mb.pie.api.None;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class UnitNoValueTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggerunitNoValueComponent.class, None.instance);
    }
}
