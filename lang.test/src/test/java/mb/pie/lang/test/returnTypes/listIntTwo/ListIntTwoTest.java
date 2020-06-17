package mb.pie.lang.test.returnTypes.listIntTwo;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static mb.pie.lang.test.util.SimpleChecker.assertTaskOutputEquals;

class ListIntTwoTest {
    @Test void test() throws Exception {
        assertTaskOutputEquals(DaggerlistIntTwoComponent.class, new ArrayList<>(Arrays.asList(1, 2)));
    }
}
