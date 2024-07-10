package mb.pie.lang.test.call.foreignFunc.twoParam;

import mb.pie.api.ExecException;
import mb.pie.lang.test.call.foreignFunc.twoParam.DaggertwoParamComponent;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class TwoParamTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggertwoParamComponent.class, true);
    }
}
