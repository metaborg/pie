package mb.pie.lang.test.returnTypes.unitExplicitValue;

import mb.pie.api.None;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class UnitExplicitValueTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggerunitExplicitValueComponent.class, None.instance);
    }
}
