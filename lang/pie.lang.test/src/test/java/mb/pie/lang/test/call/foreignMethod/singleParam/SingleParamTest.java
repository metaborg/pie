package mb.pie.lang.test.call.foreignMethod.singleParam;

import mb.pie.api.ExecException;
import mb.pie.lang.test.call.Foo;
import mb.pie.lang.test.call.foreignMethod.singleParam.DaggersingleParamComponent;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class SingleParamTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggersingleParamComponent.class, new Foo(), 5);
    }
}
