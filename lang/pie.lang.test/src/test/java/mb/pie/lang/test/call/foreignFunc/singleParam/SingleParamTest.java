package mb.pie.lang.test.call.foreignFunc.singleParam;

import mb.pie.api.ExecException;
import mb.pie.lang.test.call.foreignFunc.singleParam.DaggersingleParamComponent;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class SingleParamTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggersingleParamComponent.class, 5);
    }
}
