package mb.pie.lang.test.call.pieFunc.three;

import mb.pie.api.ExecException;
import mb.pie.lang.test.call.pieFunc.three.DaggerthreeComponent;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ThreeTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerthreeComponent.class, 3);
    }
}
