package mb.pie.lang.test.call.constructor.threeParam;

import mb.pie.api.ExecException;
import mb.pie.lang.test.call.Foo;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ThreeParamTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggerthreeParamComponent.class, new Foo());
    }
}
