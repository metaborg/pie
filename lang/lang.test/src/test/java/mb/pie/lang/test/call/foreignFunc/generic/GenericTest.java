package mb.pie.lang.test.call.foreignFunc.generic;

import mb.pie.api.ExecException;
import mb.pie.util.Tuple2;
import org.junit.jupiter.api.Test;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class GenericTest {
    @Test void test() throws ExecException {
        assertTaskOutputEquals(DaggergenericComponent.class, new Tuple2<String, Boolean>("generic", true));
    }
}
