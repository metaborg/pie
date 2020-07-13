package mb.pie.lang.test.returnTypes.listIntOne;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ListIntOneTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggerlistIntOneComponent.class, new ArrayList<>(Arrays.asList(46)));
    }
}
