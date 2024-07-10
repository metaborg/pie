package mb.pie.lang.test.call.foreignFunc.threeParam;

import mb.pie.api.ExecException;
import mb.pie.lang.test.call.foreignFunc.threeParam.DaggerthreeParamComponent;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ThreeParamTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerthreeParamComponent.class, 11);
    }
}
